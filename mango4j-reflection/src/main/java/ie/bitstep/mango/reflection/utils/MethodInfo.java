package ie.bitstep.mango.reflection.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MethodInfo {
	private final Method method;
	private final List<Class<?>> parameterTypes;

	private final List<Map<Class<? extends Annotation>, Annotation>> parameterAnnotations;

	public MethodInfo(Method method) {
		this.method = method;
		this.parameterTypes = List.of(method.getParameterTypes());
		this.parameterAnnotations = initParameterAnnotations();
	}

	private List<Map<Class<? extends Annotation>, Annotation>> initParameterAnnotations() {
		Iterator<Annotation[]> iterator = Arrays.stream(method.getParameterAnnotations()).iterator();
		List<Map<Class<? extends Annotation>, Annotation>> paramsAnnotations = new ArrayList<>();

		while (iterator.hasNext()) {
			Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();

			for (Annotation pa : iterator.next()) {
				annotations.put(pa.annotationType(), pa);
			}

			paramsAnnotations.add(annotations);
		}

		return paramsAnnotations;
	}

	public boolean equals(Object that) {
		return (this == that) || (that instanceof MethodInfo mi && this.method.equals(mi.method));
	}

	public int hashCode() {
		return method.hashCode();
	}

	public Method getMethod() {
		return method;
	}

	public List<Class<?>> getParameterTypes() {
		return parameterTypes;
	}

	public List<Map<Class<? extends Annotation>, Annotation>> getParameterAnnotations() {
		return parameterAnnotations;
	}

	public Map<Class<? extends Annotation>, Annotation> getParameterAnnotations(int argIndex) {
		return parameterAnnotations.get(argIndex);
	}

	public Optional<Annotation> findParameterAnnotation(int argIndex, Class<? extends Annotation> annotation) {
		return Optional.ofNullable(parameterAnnotations.get(argIndex).get(annotation));
	}

	public Annotation getParameterAnnotation(int argIndex, Class<? extends Annotation> annotation) {
		Optional<Annotation> result = findParameterAnnotation(argIndex, annotation);

		if (result.isEmpty()) {
			throw new IllegalArgumentException(String.format("Annotations not found: %s", annotation.getTypeName()));
		}

		return result.get();
	}

	public boolean hasParameterAnnotation(int argIndex, Class<? extends Annotation> annotation) {
		return findParameterAnnotation(argIndex, annotation).isPresent();
	}

	public boolean matches(Class<?>... matchParameterTypes) {
		if (parameterTypes.size() != matchParameterTypes.length) {
			return false;
		}

		int index = 0;

		for (Class<?> clazz : matchParameterTypes) {
			if (parameterTypes.get(index++) != clazz) {
				return false;
			}
		}

		return true;
	}
}

