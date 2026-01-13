package ie.bitstep.mango.utils.mutator;

import ie.bitstep.mango.utils.proxy.IdentityProxyResolver;
import ie.bitstep.mango.utils.proxy.ProxyResolver;
import ie.bitstep.mango.reflection.utils.ReflectionUtils;
import ie.bitstep.mango.utils.mutator.exceptions.MutatorException;
import ie.bitstep.mango.utils.mutator.exceptions.MutatorUnsupportedTypeException;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ObjectMutator {
	private boolean deep;
	private final Map<Class<?>, List<ValueMutator>> processors = new LinkedHashMap<>();
	private final Deque<Object> visited = new ArrayDeque<>();
	private final ProxyResolver proxyResolver;

	public ObjectMutator(ProxyResolver proxyResolver) {
		this.proxyResolver = proxyResolver;
		deep = true;
	}

	public ObjectMutator() {
		this(new IdentityProxyResolver());
	}

	public ObjectMutator deep() {
		deep = true;
		return this;
	}

	public ObjectMutator shallow() {
		deep = false;
		return this;
	}

	public void mutate(Object object) throws MutatorException {
		visited.push(object);
		for (Field field : getAllDeclaredFields(object.getClass())) {
			mutate(object, field);
		}
	}

	private List<Field> getAllDeclaredFields(Class<?> clazz) {
		return getAllDeclaredFields(clazz, new ArrayList<>());
	}

	private List<Field> getAllDeclaredFields(Class<?> clazz, List<Field> fields) {
		if (clazz.getSuperclass() != null) {
			fields.addAll(List.of(clazz.getDeclaredFields()));
			return getAllDeclaredFields(clazz.getSuperclass(), fields);
		}

		return fields;
	}

	public void mutate(Object object, Field field) throws MutatorException {
		if (!field.isSynthetic() && !Modifier.isFinal(field.getModifiers())) {
			try {
				Object value = ReflectionUtils.getField(object, field);

				if (value != null) {
					mutateValue(object, field, value);
				}
			}
			catch (IllegalAccessException | InvocationTargetException operationException) {
				throw new MutatorException(operationException);
			}
		}
	}

	private void mutateValue(Object object, Field field, Object value) throws MutatorException {
		if (!visited.contains(value)) {
			visited.push(value);
			if (isPrimitive(field, value)) {
				mutatePrimitive(object, field, value);
			} else {
				mutateObject(object, field, value);
			}
			visited.pop();
		}
	}

	public ObjectMutator on(Class<?> annotation, ValueMutator processor) {
		processors.computeIfAbsent(annotation, k -> newAnnotationProcessors()).add(processor);

		return this;
	}

	private List<ValueMutator> newAnnotationProcessors() {
		return new ArrayList<>();
	}

	void mutateObject(Object object, Field field, Object value) throws MutatorException {
		if (deep) {
			value = proxyResolver.resolve(value);

			if (value instanceof Map map) { // NOSONAR for parametrized type for this generic map (unsfe conversion)
				mutateMap(object, field, map);
			} else if (value instanceof Collection<?> collection) {
				mutateCollection(object, field, (Collection<Object>) collection);
			} else if (value instanceof byte[]) {
				throw new MutatorUnsupportedTypeException(field);
			} else if (value.getClass().isArray()) {
				throw new MutatorUnsupportedTypeException(field);
			} else {
				mutate(value);
			}
		}
	}

	private void mutateCollection(Object object, Field field, Collection<Object> collection) throws MutatorException {
		ArrayList<Object> tmp = new ArrayList<>();

		CollectionUtils.addAll(tmp, collection);
		collection.clear();

		for (Object v : tmp) {
			Object n = mutateEntry(object, field, v);

			if (!Objects.equals(n, v)) {
				collection.add(n);
			} else {
				collection.add(v);
			}
		}
	}

	private void mutateMap(Object object, Field field, Map<Object, Object> map) throws MutatorException {
		for (Map.Entry<Object, Object> v : map.entrySet()) {
			Object result = mutateEntry(object, field, v.getValue());

			if (!Objects.equals(result, v.getValue())) {
				map.put(v.getKey(), result);
			}
		}
	}

	private Object mutateEntry(Object object, Field field, Object value) throws MutatorException {
		if (value != null) {
			if (isPrimitive(field, value)) {
				return processPrimitive(field, value);
			} else {
				mutateObject(object, field, value);
			}
		}

		return value;
	}

	private void mutatePrimitive(Object object, Field field, Object value) {
		Object result = value;

		result = processPrimitive(field, result);

		if (!Objects.equals(result, value)) {
			try {
				ReflectionUtils.setField(object, field, result);
			} catch (Exception e) {
				throw new MutatorException(e);
			}
		}
	}

	private Object processPrimitive(Field field, Object value) {
		Object result = value;

		Annotation[] annotations = field.getAnnotations();
		for (Annotation annotation : annotations) {
			Class<? extends Annotation> clazz = annotation.annotationType();
			List<ValueMutator> mutators = processors.get(clazz);

			if (!CollectionUtils.isEmpty(mutators)) {
				for (ValueMutator processor : processors.get(clazz)) {
					result = processor.process(annotation, result);
				}
			}
		}

		return result;
	}

	/**
	 * Is this value a primitive type, String, Boolean Number or Enum
	 *
	 * @param value instance to check type of
	 * @return true if instance of String, Boolean or Number, or Enum
	 */
	private boolean isPrimitive(Field f, Object value) {
		return f.getType().isPrimitive()
			|| Enum.class.isAssignableFrom(value.getClass())
			|| String.class.isAssignableFrom(value.getClass())
			|| Boolean.class.isAssignableFrom(value.getClass())
			|| Number.class.isAssignableFrom(value.getClass())
		;
	}
}
