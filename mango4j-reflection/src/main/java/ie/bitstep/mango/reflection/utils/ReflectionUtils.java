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

	private ReflectionUtils() { // NOSONAR
	}

	public static boolean isCoreType(Class<?> type) {
		return CORE_TYPES.contains(type);
	}

	public static boolean isCoreType(Object o) {
		return CORE_TYPES.contains(o.getClass());
	}

	public static ClassInfo getClassInfo(Class<?> clazz) {
		return CLASS_INFO_MAP.computeIfAbsent(clazz, ClassInfo::new);
	}

	public static ClassInfo getClassInfo(Object o) {
		return CLASS_INFO_MAP.computeIfAbsent(o.getClass(), ClassInfo::new);
	}

	public static void setField(Object target, String name, Object value) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(target.getClass(), name);
		pa.set(target, value);
	}

	public static void forceSetField(Object target, String name, Object value) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(target.getClass(), name);
		pa.forceSet(target, value);
	}

	public static void setField(Class<?> clazz, String name, Object value) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(clazz, name);
		pa.set(clazz, value);
	}

	public static void forceSetField(Class<?> clazz, String name, Object value) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(clazz, name);
		pa.forceSet(clazz, value);
	}

	public static void setField(Object target, Field field, Object value) throws InvocationTargetException, IllegalAccessException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(field.getDeclaringClass(), field);
		pa.set(target, value);
	}

	public static void forceSetField(Object target, Field field, Object value) throws InvocationTargetException, IllegalAccessException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(field.getDeclaringClass(), field);
		pa.forceSet(target, value);
	}

	public static Object getField(Object target, String name) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(target.getClass(), name);
		return pa.get(target);
	}

	public static Object forceGetField(Object target, String name) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(target.getClass(), name);
		return pa.forceGet(target);
	}

	public static Object getField(Class<?> clazz, String name) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(clazz, name);
		return pa.get(clazz);
	}

	public static Object forceGetField(Class<?> clazz, String name) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(clazz, name);
		return pa.forceGet(clazz);
	}

	public static Object getField(Object target, Field field) throws InvocationTargetException, IllegalAccessException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(field.getDeclaringClass(), field);
		return pa.get(target);
	}

	public static Object forceGetField(Object target, Field field) throws InvocationTargetException, IllegalAccessException {
		PropertyAccessor<Object> pa = new PropertyAccessor<>(field.getDeclaringClass(), field);
		return pa.forceGet(target);
	}

	public static Method getMethod(Class<?> clazz, String name) {
		return CLASS_INFO_MAP.computeIfAbsent(clazz, ClassInfo::new).getMethod(name);
	}

	public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		return CLASS_INFO_MAP.computeIfAbsent(clazz, ClassInfo::new).getMethod(name, parameterTypes);
	}

	public static List<MethodInfo> getMethodInfoByAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
		return CLASS_INFO_MAP.computeIfAbsent(clazz, ClassInfo::new).getMethodInfoByAnnotation(annotation);
	}

	public static List<MethodInfo> getMethodInfoByName(Class<?> clazz, String name) {
		return CLASS_INFO_MAP.computeIfAbsent(clazz, ClassInfo::new).getMethodInfoByName(name);
	}
}

