package ie.bitstep.mango.reflection.utils;

import ie.bitstep.mango.reflection.accessors.PropertyAccessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClassInfo {
	private final Class<?> clazz;
	private final Map<String, List<MethodInfo>> methods = new LinkedHashMap<>();
	private final Map<Class<? extends Annotation>, List<MethodInfo>> methodsByAnnotation = new LinkedHashMap<>();
	private final Map<String, PropertyAccessor<?>> accessors = new LinkedHashMap<>();

	/**
	 * Creates metadata for the supplied class.
	 *
	 * @param clazz the class to inspect
	 */
	public ClassInfo(Class<?> clazz) {
		this.clazz = clazz;
		populateMethodCache();
		populateAccessorCache();
	}

	/**
	 * Returns the inspected class.
	 *
	 * @return the class
	 */
	public Class<?> getClazz() {
		return clazz;
	}

	/**
	 * Compares class metadata by underlying class.
	 *
	 * @param that the other object
	 * @return true when equal
	 */
	public boolean equals(Object that) {
		return (this == that) || (that instanceof ClassInfo ci && this.clazz.equals(ci.clazz));
	}

	/**
	 * Returns the hash code of the underlying class.
	 *
	 * @return the hash code
	 */
	public int hashCode() {
		return clazz.hashCode();
	}

	/**
	 * Populates method caches by name and annotation.
	 */
	private void populateMethodCache() {
		for (Method m : clazz.getDeclaredMethods()) {
			methods.computeIfAbsent(m.getName(), name -> new ArrayList<>()).add(new MethodInfo(m));
			for (Annotation annotation : m.getAnnotations()) {
				methodsByAnnotation.computeIfAbsent(annotation.annotationType(), method -> new ArrayList<>()).add(new MethodInfo(m));
			}
		}
	}

	/**
	 * Populates property accessors for declared fields.
	 */
	private void populateAccessorCache() {
		for (Field f : clazz.getDeclaredFields()) {
			accessors.computeIfAbsent(f.getName(), name -> new PropertyAccessor<>(this, f)); // NOSONAR
		}
	}

	/**
	 * Returns all cached property accessors.
	 *
	 * @return the property accessors
	 */
	public Collection<PropertyAccessor<?>> getPropertyAccessors() { // NOSONAR
		return accessors.values();
	}

	/**
	 * Returns the accessor for a specific field.
	 *
	 * @param fieldName the field name
	 * @return the property accessor or null
	 */
	@SuppressWarnings("java:S1452")
	public PropertyAccessor<?> getPropertyAccessor(String fieldName) {
		return accessors.get(fieldName);
	}

	/**
	 * Returns a no-arg method by name.
	 *
	 * @param name the method name
	 * @return the method or null when not found
	 */
	public Method getMethod(String name) {
		List<MethodInfo> methodInfo = methods.get(name);

		if (methodInfo != null) {
			for (MethodInfo mi : methodInfo) {
				if (mi.getParameterTypes().isEmpty()) {
					return mi.getMethod();
				}
			}
		}

		return null;
	}

	/**
	 * Returns a method by name and parameter types.
	 *
	 * @param name the method name
	 * @param parameterTypes parameter types to match
	 * @return the method or null when not found
	 */
	public Method getMethod(String name, Class<?>... parameterTypes) {
		List<MethodInfo> methodInfo = methods.get(name);

		if (methodInfo != null) {
			for (MethodInfo mi : methodInfo) {
				if (mi.matches(parameterTypes)) {
					return mi.getMethod();
				}
			}
		}

		return null;
	}

	/**
	 * Returns methods annotated with the supplied annotation.
	 *
	 * @param annotation the annotation type
	 * @return list of method info
	 */
	public List<MethodInfo> getMethodInfoByAnnotation(Class<? extends Annotation> annotation) {
		if (methodsByAnnotation.containsKey(annotation)) {
			return methodsByAnnotation.get(annotation);
		}

		return Collections.emptyList();
	}

	/**
	 * Returns methods matching the supplied name.
	 *
	 * @param name the method name
	 * @return list of method info
	 */
	public List<MethodInfo> getMethodInfoByName(String name) {
		if (methods.containsKey(name)) {
			return methods.get(name);
		}

		return Collections.emptyList();
	}
}
