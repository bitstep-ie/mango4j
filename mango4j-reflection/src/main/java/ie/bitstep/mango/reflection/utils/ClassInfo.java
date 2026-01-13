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

	public ClassInfo(Class<?> clazz) {
		this.clazz = clazz;
		populateMethodCache();
		populateAccessorCache();
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public boolean equals(Object that) {
		return (this == that) || (that instanceof ClassInfo ci && this.clazz.equals(ci.clazz));
	}

	public int hashCode() {
		return clazz.hashCode();
	}

	private void populateMethodCache() {
		for (Method m : clazz.getDeclaredMethods()) {
			methods.computeIfAbsent(m.getName(), name -> new ArrayList<>()).add(new MethodInfo(m));
			for (Annotation annotation : m.getAnnotations()) {
				methodsByAnnotation.computeIfAbsent(annotation.annotationType(), method -> new ArrayList<>()).add(new MethodInfo(m));
			}
		}
	}

	private void populateAccessorCache() {
		for (Field f : clazz.getDeclaredFields()) {
			accessors.computeIfAbsent(f.getName(), name -> new PropertyAccessor<>(this, f)); // NOSONAR
		}
	}

	public Collection<PropertyAccessor<?>> getPropertyAccessors() { // NOSONAR
		return accessors.values();
	}

	@SuppressWarnings("java:S1452")
	public PropertyAccessor<?> getPropertyAccessor(String fieldName) {
		return accessors.get(fieldName);
	}

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

	public List<MethodInfo> getMethodInfoByAnnotation(Class<? extends Annotation> annotation) {
		if (methodsByAnnotation.containsKey(annotation)) {
			return methodsByAnnotation.get(annotation);
		}

		return Collections.emptyList();
	}

	public List<MethodInfo> getMethodInfoByName(String name) {
		if (methods.containsKey(name)) {
			return methods.get(name);
		}

		return Collections.emptyList();
	}
}
