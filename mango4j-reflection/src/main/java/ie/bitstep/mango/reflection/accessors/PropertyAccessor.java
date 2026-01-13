package ie.bitstep.mango.reflection.accessors;

import ie.bitstep.mango.reflection.utils.ClassInfo;
import ie.bitstep.mango.reflection.utils.MethodInfo;
import ie.bitstep.mango.reflection.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ie.bitstep.mango.reflection.utils.StringUtils.capitalize;

public class PropertyAccessor<T> {
	private Class<?> clazz;
	private String fieldName;
	private Field field;
	private Method getter;
	private Method setter;
	private final Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();

	public PropertyAccessor(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		this(getDeclaredField(clazz, fieldName));
	}

	public PropertyAccessor(Class<?> clazz, Field field) {
		init(null, clazz, field);
	}

	public PropertyAccessor(ClassInfo classInfo, Field field) {
		init(classInfo, classInfo.getClazz(), field);
	}

	public PropertyAccessor(Field field) {
		this(field.getDeclaringClass(), field);
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Field getField() {
		return field;
	}

	public Method getGetter() {
		return getter;
	}

	public Method getSetter() {
		return setter;
	}

	static Field getDeclaredField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class<?> superClazz = clazz.getSuperclass();
			if (superClazz != Object.class) { // stop at Object
				return getDeclaredField(superClazz, fieldName);
			}

			throw e;
		}
	}

	private void init(ClassInfo classInfo, Class<?> clazz, Field field) {
		this.getter = null;
		this.setter = null;
		this.clazz = clazz;
		this.field = field;
		this.fieldName = field.getName();
		ClassInfo ci = getClassInfo(classInfo, clazz);

		// populate fast annotation maps
		populateAnnotations(field);

		initialiseGetter(ci);
		initialiseSetter(ci);

		// intialise getters/setters from @Accessor annotation (if present)
		initialiseAccessors(clazz);

		// intialise getters/setters from @PropertyGetter/@PropertySetter annotation, over-riding any @Accessor settings (if present)
		initialisePropertyGetter(ci);
		initialisePropertySetter(ci);
	}

	private void initialiseGetter(ClassInfo classInfo) {
		// Lombok uses get.. for Boolean
		getter = classInfo.getMethod("get" + capitalize(fieldName));

		// Lombok uses is.. for boolean
		if (getter == null && field.getType().isAssignableFrom(boolean.class)) {
			getter = classInfo.getMethod("is" + capitalize(fieldName));
		}
	}

	private void initialiseSetter(ClassInfo classInfo) {
		setter = classInfo.getMethod("set" + capitalize(fieldName), field.getType());
	}

	private void initialisePropertyGetter(ClassInfo classInfo) {
		List<MethodInfo> methodInfos = classInfo.getMethodInfoByAnnotation(PropertyGetter.class);

		for (MethodInfo methodInfo : methodInfos) {
			PropertyGetter propertyGetter = methodInfo.getMethod().getDeclaredAnnotation(PropertyGetter.class);

			if (propertyGetter.value().equals(fieldName)) {
				getter = methodInfo.getMethod();
			}
		}
	}

	private void initialisePropertySetter(ClassInfo classInfo) {
		List<MethodInfo> methodInfos = classInfo.getMethodInfoByAnnotation(PropertySetter.class);

		for (MethodInfo methodInfo : methodInfos) {
			PropertySetter propertyGetter = methodInfo.getMethod().getDeclaredAnnotation(PropertySetter.class);

			if (propertyGetter.value().equals(fieldName)) {
				setter = methodInfo.getMethod();
			}
		}
	}

	private void initialiseAccessors(Class<?> clazz) {
		Accessor accessor = field.getAnnotation(Accessor.class);

		if (accessor != null) {
			if (!accessor.getter().isEmpty()) {
				try {
					getter = clazz.getMethod(accessor.getter());
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(e); // NOSONAR: accessor not found
				}
			}

			if (!accessor.setter().isEmpty()) {
				try {
					setter = clazz.getMethod(accessor.setter(), field.getType());
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(e); // NOSONAR: accessor not found
				}
			}
		}
	}

	private void populateAnnotations(Class<? extends Annotation> type) {
		for (Annotation a : type.getAnnotations()) {
			if (!a.annotationType().getPackageName().startsWith("java.lang")) {
				if (!annotations.containsKey(a.annotationType())) { // NOSONAR
					annotations.put(a.annotationType(), a);
					populateAnnotations(a.annotationType());
				}
			}
		}
	}

	private void populateAnnotations(Field type) {
		for (Annotation a : type.getAnnotations()) {
			if (!a.annotationType().getPackageName().startsWith("java.lang")) {
				if (!annotations.containsKey(a.annotationType())) { // NOSONAR
					annotations.put(a.annotationType(), a);
					populateAnnotations(a.annotationType());
				}
			}
		}
	}

	private static ClassInfo getClassInfo(ClassInfo classInfo, Class<?> clazz) {
		return classInfo != null ? classInfo : ReflectionUtils.getClassInfo(clazz);
	}

	public Map<Class<? extends Annotation>, Annotation> getAnnotations() {
		return annotations;
	}

	public Annotation getAnnotation(Class<? extends Annotation> annotation) {
		return annotations.get(annotation);
	}

	public boolean hasAnnotation(Class<? extends Annotation> annotation) {
		return annotations.containsKey(annotation);
	}

	public boolean isCoreType() {
		return ReflectionUtils.isCoreType(field.getType());
	}

	@SuppressWarnings("java:S3011")
	public void makeAccessible() {
		field.setAccessible(true); // NOSNOAR: required for classes we don't control
	}

	@SuppressWarnings("unchecked")
	public T get(Object o) throws IllegalAccessException, InvocationTargetException {
		if (getter != null) {
			return (T) getter.invoke(o);
		}

		return (T) field.get(o);
	}

	@SuppressWarnings("unchecked")
	public T forceGet(Object o) throws IllegalAccessException, InvocationTargetException {
		if (getter != null) {
			return (T) getter.invoke(o);
		}
		else {

			try {
				return (T) field.get(o);
			} catch (Exception e) {
				makeAccessible();
				return (T) field.get(o);
			}
		}
	}

	public void set(Object o, T value) throws InvocationTargetException, IllegalAccessException {
		if (setter != null) {
			setter.invoke(o, value);
		} else {
			field.set(o, value); // NOSONAR
		}
	}

	public void forceSet(Object o, T value) throws InvocationTargetException, IllegalAccessException {
		if (setter != null) {
			setter.invoke(o, value);
		} else {
			try {
				field.set(o, value); // NOSONAR
			}
			catch (Exception e) {
				makeAccessible();
				field.set(o, value); // NOSONAR
			}
		}
	}
}

