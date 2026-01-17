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

	/**
	 * Creates metadata for the supplied method.
	 *
	 * @param method the method to inspect
	 */
	public MethodInfo(Method method) {
		this.method = method;
		this.parameterTypes = List.of(method.getParameterTypes());
		this.parameterAnnotations = initParameterAnnotations();
	}

	/**
	 * Builds a list of annotation maps for each parameter.
	 *
	 * @return the parameter annotations
	 */
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

	/**
	 * Compares method metadata by underlying method.
	 *
	 * @param that the other object
	 * @return true when equal
	 */
	public boolean equals(Object that) {
		return (this == that) || (that instanceof MethodInfo mi && this.method.equals(mi.method));
	}

	/**
	 * Returns the hash code of the underlying method.
	 *
	 * @return the hash code
	 */
	public int hashCode() {
		return method.hashCode();
	}

	/**
	 * Returns the underlying method.
	 *
	 * @return the method
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * Returns the parameter types for the method.
	 *
	 * @return the parameter types
	 */
	public List<Class<?>> getParameterTypes() {
		return parameterTypes;
	}

	/**
	 * Returns all parameter annotations.
	 *
	 * @return parameter annotations by index
	 */
	public List<Map<Class<? extends Annotation>, Annotation>> getParameterAnnotations() {
		return parameterAnnotations;
	}

	/**
	 * Returns the annotation map for the specified parameter.
	 *
	 * @param argIndex parameter index
	 * @return annotations for the parameter
	 */
	public Map<Class<? extends Annotation>, Annotation> getParameterAnnotations(int argIndex) {
		return parameterAnnotations.get(argIndex);
	}

	/**
	 * Finds a specific annotation on a parameter.
	 *
	 * @param argIndex parameter index
	 * @param annotation annotation type
	 * @return the annotation when present
	 */
	public Optional<Annotation> findParameterAnnotation(int argIndex, Class<? extends Annotation> annotation) {
		return Optional.ofNullable(parameterAnnotations.get(argIndex).get(annotation));
	}

	/**
	 * Gets a specific annotation on a parameter or throws.
	 *
	 * @param argIndex parameter index
	 * @param annotation annotation type
	 * @return the annotation
	 * @throws IllegalArgumentException when not found
	 */
	public Annotation getParameterAnnotation(int argIndex, Class<? extends Annotation> annotation) {
		Optional<Annotation> result = findParameterAnnotation(argIndex, annotation);

		if (result.isEmpty()) {
			throw new IllegalArgumentException(String.format("Annotations not found: %s", annotation.getTypeName()));
		}

		return result.get();
	}

	/**
	 * Checks whether a parameter has the given annotation.
	 *
	 * @param argIndex parameter index
	 * @param annotation annotation type
	 * @return true when present
	 */
	public boolean hasParameterAnnotation(int argIndex, Class<? extends Annotation> annotation) {
		return findParameterAnnotation(argIndex, annotation).isPresent();
	}

	/**
	 * Matches the method's parameter types against supplied types.
	 *
	 * @param matchParameterTypes types to match
	 * @return true when types match in order
	 */
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
