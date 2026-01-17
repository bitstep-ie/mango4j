package ie.bitstep.mango.reflection.utils;

import ie.bitstep.mango.reflection.accessors.PropertyAccessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtils {
	private static final Map<Class<?>, ClassInfo> CLASS_INFO_MAP = new ConcurrentHashMap<>();

	private static final Set<Class<?>> CORE_TYPES = new HashSet<>();

	static {
		CORE_TYPES.add(Enum.class);
		CORE_TYPES.add(String.class);
		CORE_TYPES.add(Boolean.class);
		CORE_TYPES.add(Long.class);
		CORE_TYPES.add(Integer.class);
		CORE_TYPES.add(Float.class);
		CORE_TYPES.add(Double.class);
	}

	/**
	 * Prevents instantiation.
	 */
	private ReflectionUtils() { // NOSONAR
	}

	/**
	 * Checks whether a class is considered a core type.
	 *
	 * @param type the class to check
	 * @return true if the class is a core type
	 */
	public static boolean isCoreType(Class<?> type) {
		return CORE_TYPES.contains(type);
	}

	/**
	 * Checks whether an object's class is considered a core type.
	 *
	 * @param o the object to check
	 * @return true if the object's class is a core type
	 */
	public static boolean isCoreType(Object o) {
		return CORE_TYPES.contains(o.getClass());
	}

	/**
	 * Returns cached class metadata for the supplied class.
	 *
	 * @param clazz the class to inspect
	 * @return the cached ClassInfo
	 */
	public static ClassInfo getClassInfo(Class<?> clazz) {
		return CLASS_INFO_MAP.computeIfAbsent(clazz, ClassInfo::new);
	}

	/**
	 * Returns cached class metadata for the supplied object.
	 *
	 * @param o the object to inspect
	 * @return the cached ClassInfo
	 */
	public static ClassInfo getClassInfo(Object o) {
		return CLASS_INFO_MAP.computeIfAbsent(o.getClass(), ClassInfo::new);
	}

	/**
	 * Sets a field value on an instance by field name.
	 *
	 * @param target the instance to modify
	 * @param name the field name
	 * @param value the value to set
	 * @throws InvocationTargetException when accessor invocation fails
	 * @throws IllegalAccessException when access is denied
	 * @throws NoSuchFieldException when the field does not exist
	 */
	public static void setField(Object target, String name, Object value) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(target.getClass(), name);
		pa.set(target, value);
	}

	/**
	 * Forces a field value on an instance by field name.
	 *
	 * @param target the instance to modify
	 * @param name the field name
	 * @param value the value to set
	 * @throws InvocationTargetException when accessor invocation fails
	 * @throws IllegalAccessException when access is denied
	 * @throws NoSuchFieldException when the field does not exist
	 */
	public static void forceSetField(Object target, String name, Object value) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(target.getClass(), name);
		pa.forceSet(target, value);
	}

	/**
	 * Sets a static field value on a class by field name.
	 *
	 * @param clazz the class to modify
	 * @param name the field name
	 * @param value the value to set
	 * @throws InvocationTargetException when accessor invocation fails
	 * @throws IllegalAccessException when access is denied
	 * @throws NoSuchFieldException when the field does not exist
	 */
	public static void setField(Class<?> clazz, String name, Object value) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(clazz, name);
		pa.set(clazz, value);
	}

	/**
	 * Forces a static field value on a class by field name.
	 *
	 * @param clazz the class to modify
	 * @param name the field name
	 * @param value the value to set
	 * @throws InvocationTargetException when accessor invocation fails
	 * @throws IllegalAccessException when access is denied
	 * @throws NoSuchFieldException when the field does not exist
	 */
	public static void forceSetField(Class<?> clazz, String name, Object value) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(clazz, name);
		pa.forceSet(clazz, value);
	}

	/**
	 * Sets a field value on an instance by field reference.
	 *
	 * @param target the instance to modify
	 * @param field the field reference
	 * @param value the value to set
	 * @throws InvocationTargetException when accessor invocation fails
	 * @throws IllegalAccessException when access is denied
	 */
	public static void setField(Object target, Field field, Object value) throws InvocationTargetException, IllegalAccessException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(field.getDeclaringClass(), field);
		pa.set(target, value);
	}

	/**
	 * Forces a field value on an instance by field reference.
	 *
	 * @param target the instance to modify
	 * @param field the field reference
	 * @param value the value to set
	 * @throws InvocationTargetException when accessor invocation fails
	 * @throws IllegalAccessException when access is denied
	 */
	public static void forceSetField(Object target, Field field, Object value) throws InvocationTargetException, IllegalAccessException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(field.getDeclaringClass(), field);
		pa.forceSet(target, value);
	}

	/**
	 * Gets a field value from an instance by field name.
	 *
	 * @param target the instance to read from
	 * @param name the field name
	 * @return the field value
	 * @throws InvocationTargetException when accessor invocation fails
	 * @throws IllegalAccessException when access is denied
	 * @throws NoSuchFieldException when the field does not exist
	 */
	public static Object getField(Object target, String name) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(target.getClass(), name);
		return pa.get(target);
	}

	/**
	 * Forces a field read from an instance by field name.
	 *
	 * @param target the instance to read from
	 * @param name the field name
	 * @return the field value
	 * @throws InvocationTargetException when accessor invocation fails
	 * @throws IllegalAccessException when access is denied
	 * @throws NoSuchFieldException when the field does not exist
	 */
	public static Object forceGetField(Object target, String name) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(target.getClass(), name);
		return pa.forceGet(target);
	}

	/**
	 * Gets a static field value from a class by field name.
	 *
	 * @param clazz the class to read from
	 * @param name the field name
	 * @return the field value
	 * @throws InvocationTargetException when accessor invocation fails
	 * @throws IllegalAccessException when access is denied
	 * @throws NoSuchFieldException when the field does not exist
	 */
	public static Object getField(Class<?> clazz, String name) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(clazz, name);
		return pa.get(clazz);
	}

	/**
	 * Forces a static field read from a class by field name.
	 *
	 * @param clazz the class to read from
	 * @param name the field name
	 * @return the field value
	 * @throws InvocationTargetException when accessor invocation fails
	 * @throws IllegalAccessException when access is denied
	 * @throws NoSuchFieldException when the field does not exist
	 */
	public static Object forceGetField(Class<?> clazz, String name) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(clazz, name);
		return pa.forceGet(clazz);
	}

	/**
	 * Gets a field value from an instance by field reference.
	 *
	 * @param target the instance to read from
	 * @param field the field reference
	 * @return the field value
	 * @throws InvocationTargetException when accessor invocation fails
	 * @throws IllegalAccessException when access is denied
	 */
	public static Object getField(Object target, Field field) throws InvocationTargetException, IllegalAccessException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(field.getDeclaringClass(), field);
		return pa.get(target);
	}

	/**
	 * Forces a field read from an instance by field reference.
	 *
	 * @param target the instance to read from
	 * @param field the field reference
	 * @return the field value
	 * @throws InvocationTargetException when accessor invocation fails
	 * @throws IllegalAccessException when access is denied
	 */
	public static Object forceGetField(Object target, Field field) throws InvocationTargetException, IllegalAccessException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(field.getDeclaringClass(), field);
		return pa.forceGet(target);
	}

	/**
	 * Returns a method by name for the supplied class.
	 *
	 * @param clazz the class to inspect
	 * @param name the method name
	 * @return the method or null when not found
	 */
	public static Method getMethod(Class<?> clazz, String name) {
		return CLASS_INFO_MAP.computeIfAbsent(clazz, ClassInfo::new).getMethod(name);
	}

	/**
	 * Returns a method by name and parameter types for the supplied class.
	 *
	 * @param clazz the class to inspect
	 * @param name the method name
	 * @param parameterTypes parameter types
	 * @return the method or null when not found
	 */
	public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		return CLASS_INFO_MAP.computeIfAbsent(clazz, ClassInfo::new).getMethod(name, parameterTypes);
	}

	/**
	 * Returns method metadata for methods annotated with the supplied annotation.
	 *
	 * @param clazz the class to inspect
	 * @param annotation the annotation type
	 * @return matching method info
	 */
	public static List<MethodInfo> getMethodInfoByAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
		return CLASS_INFO_MAP.computeIfAbsent(clazz, ClassInfo::new).getMethodInfoByAnnotation(annotation);
	}

	/**
	 * Returns method metadata for methods matching the supplied name.
	 *
	 * @param clazz the class to inspect
	 * @param name the method name
	 * @return matching method info
	 */
	public static List<MethodInfo> getMethodInfoByName(Class<?> clazz, String name) {
		return CLASS_INFO_MAP.computeIfAbsent(clazz, ClassInfo::new).getMethodInfoByName(name);
	}
}
