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

	/**
	 * Creates a mutator with a custom proxy resolver.
	 *
	 * @param proxyResolver the proxy resolver to use
	 */
	public ObjectMutator(ProxyResolver proxyResolver) {
		this.proxyResolver = proxyResolver;
		deep = true;
	}

	/**
	 * Creates a mutator using an identity proxy resolver.
	 */
	public ObjectMutator() {
		this(new IdentityProxyResolver());
	}

	/**
	 * Enables deep mutation of nested objects.
	 *
	 * @return this mutator
	 */
	public ObjectMutator deep() {
		deep = true;
		return this;
	}

	/**
	 * Disables deep mutation and only processes direct fields.
	 *
	 * @return this mutator
	 */
	public ObjectMutator shallow() {
		deep = false;
		return this;
	}

	/**
	 * Mutates all eligible fields on the supplied object.
	 *
	 * @param object the object to mutate
	 * @throws MutatorException when mutation fails
	 */
	public void mutate(Object object) throws MutatorException {
		visited.push(object);
		for (Field field : getAllDeclaredFields(object.getClass())) {
			mutate(object, field);
		}
	}

	/**
	 * Returns all declared fields from a class hierarchy.
	 *
	 * @param clazz the class to inspect
	 * @return the list of fields
	 */
	private List<Field> getAllDeclaredFields(Class<?> clazz) {
		return getAllDeclaredFields(clazz, new ArrayList<>());
	}

	/**
	 * Recursively collects declared fields up the class hierarchy.
	 *
	 * @param clazz the class to inspect
	 * @param fields the accumulator list
	 * @return the list of fields
	 */
	private List<Field> getAllDeclaredFields(Class<?> clazz, List<Field> fields) {
		if (clazz.getSuperclass() != null) {
			fields.addAll(List.of(clazz.getDeclaredFields()));
			return getAllDeclaredFields(clazz.getSuperclass(), fields);
		}

		return fields;
	}

	/**
	 * Mutates a specific field on the supplied object.
	 *
	 * @param object the target object
	 * @param field the field to mutate
	 * @throws MutatorException when mutation fails
	 */
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

	/**
	 * Mutates a field value, avoiding cycles via the visited stack.
	 *
	 * @param object the target object
	 * @param field the field being mutated
	 * @param value the current field value
	 * @throws MutatorException when mutation fails
	 */
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

	/**
	 * Registers a mutator for the supplied annotation type.
	 *
	 * @param annotation the annotation type
	 * @param processor the mutator to apply
	 * @return this mutator
	 */
	public ObjectMutator on(Class<?> annotation, ValueMutator processor) {
		processors.computeIfAbsent(annotation, k -> newAnnotationProcessors()).add(processor);

		return this;
	}

	/**
	 * Creates a new list of annotation processors.
	 *
	 * @return a new list instance
	 */
	private List<ValueMutator> newAnnotationProcessors() {
		return new ArrayList<>();
	}

	/**
	 * Mutates a non-primitive field value.
	 *
	 * @param object the target object
	 * @param field the field being mutated
	 * @param value the current field value
	 * @throws MutatorException when mutation fails
	 */
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

	/**
	 * Mutates elements in a collection, replacing values when mutated.
	 *
	 * @param object the target object
	 * @param field the field being mutated
	 * @param collection the collection to mutate
	 * @throws MutatorException when mutation fails
	 */
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

	/**
	 * Mutates values in a map, replacing values when mutated.
	 *
	 * @param object the target object
	 * @param field the field being mutated
	 * @param map the map to mutate
	 * @throws MutatorException when mutation fails
	 */
	private void mutateMap(Object object, Field field, Map<Object, Object> map) throws MutatorException {
		for (Map.Entry<Object, Object> v : map.entrySet()) {
			Object result = mutateEntry(object, field, v.getValue());

			if (!Objects.equals(result, v.getValue())) {
				map.put(v.getKey(), result);
			}
		}
	}

	/**
	 * Mutates a single entry value.
	 *
	 * @param object the target object
	 * @param field the field being mutated
	 * @param value the entry value
	 * @return the possibly mutated value
	 * @throws MutatorException when mutation fails
	 */
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

	/**
	 * Mutates a primitive field value in-place on the object.
	 *
	 * @param object the target object
	 * @param field the field being mutated
	 * @param value the current field value
	 */
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

	/**
	 * Applies registered mutators for annotations on the field.
	 *
	 * @param field the field being mutated
	 * @param value the current value
	 * @return the mutated value
	 */
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
