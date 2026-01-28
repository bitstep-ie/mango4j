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

	/**
	 * Creates an accessor for the field name on the class.
	 *
	 * @param clazz the declaring class
	 * @param fieldName the field name
	 * @throws NoSuchFieldException when the field does not exist
	 */
	public PropertyAccessor(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		this(getDeclaredField(clazz, fieldName));
	}

	/**
	 * Creates an accessor for the supplied field.
	 *
	 * @param clazz the declaring class
	 * @param field the field to access
	 */
	public PropertyAccessor(Class<?> clazz, Field field) {
		init(null, clazz, field);
	}

	/**
	 * Creates an accessor for the supplied field using cached class info.
	 *
	 * @param classInfo the cached class info
	 * @param field the field to access
	 */
	public PropertyAccessor(ClassInfo classInfo, Field field) {
		init(classInfo, classInfo.getClazz(), field);
	}

	/**
	 * Creates an accessor for the supplied field.
	 *
	 * @param field the field to access
	 */
	public PropertyAccessor(Field field) {
		this(field.getDeclaringClass(), field);
	}

	/**
	 * Returns the declaring class.
	 *
	 * @return the class
	 */
	public Class<?> getClazz() {
		return clazz;
	}

	/**
	 * Returns the field name.
	 *
	 * @return the field name
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Returns the field.
	 *
	 * @return the field
	 */
	public Field getField() {
		return field;
	}

	/**
	 * Returns the getter method, if present.
	 *
	 * @return the getter or null
	 */
	public Method getGetter() {
		return getter;
	}

	/**
	 * Returns the setter method, if present.
	 *
	 * @return the setter or null
	 */
	public Method getSetter() {
		return setter;
	}

	/**
	 * Finds a declared field by name on the class or its superclasses.
	 *
	 * @param clazz the declaring class
	 * @param fieldName the field name
	 * @return the field
	 * @throws NoSuchFieldException when not found
	 */
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

	/**
	 * Initializes accessor state.
	 *
	 * @param classInfo optional cached class info
	 * @param clazz the declaring class
	 * @param field the field to access
	 */
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

	/**
	 * Resolves a getter using JavaBeans naming conventions.
	 *
	 * @param classInfo cached class info
	 */
	private void initialiseGetter(ClassInfo classInfo) {
		// Lombok uses get.. for Boolean
		getter = classInfo.getMethod("get" + capitalize(fieldName));

		// Lombok uses is.. for boolean
		if (getter == null && field.getType().isAssignableFrom(boolean.class)) {
			getter = classInfo.getMethod("is" + capitalize(fieldName));
		}
	}

	/**
	 * Resolves a setter using JavaBeans naming conventions.
	 *
	 * @param classInfo cached class info
	 */
	private void initialiseSetter(ClassInfo classInfo) {
		setter = classInfo.getMethod("set" + capitalize(fieldName), field.getType());
	}

	/**
	 * Resolves a getter using {@link PropertyGetter} annotations.
	 *
	 * @param classInfo cached class info
	 */
	private void initialisePropertyGetter(ClassInfo classInfo) {
		List<MethodInfo> methodInfos = classInfo.getMethodInfoByAnnotation(PropertyGetter.class);

		for (MethodInfo methodInfo : methodInfos) {
			PropertyGetter propertyGetter = methodInfo.getMethod().getDeclaredAnnotation(PropertyGetter.class);

			if (propertyGetter.value().equals(fieldName)) {
				getter = methodInfo.getMethod();
			}
		}
	}

	/**
	 * Resolves a setter using {@link PropertySetter} annotations.
	 *
	 * @param classInfo cached class info
	 */
	private void initialisePropertySetter(ClassInfo classInfo) {
		List<MethodInfo> methodInfos = classInfo.getMethodInfoByAnnotation(PropertySetter.class);

		for (MethodInfo methodInfo : methodInfos) {
			PropertySetter propertyGetter = methodInfo.getMethod().getDeclaredAnnotation(PropertySetter.class);

			if (propertyGetter.value().equals(fieldName)) {
				setter = methodInfo.getMethod();
			}
		}
	}

	/**
	 * Resolves accessors using {@link Accessor} annotation values.
	 *
	 * @param clazz the declaring class
	 */
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

	/**
	 * Recursively collects annotations from annotation types.
	 *
	 * @param type the annotation type
	 */
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

	/**
	 * Collects annotations from the field and its meta-annotations.
	 *
	 * @param type the field to inspect
	 */
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

	/**
	 * Returns cached class info or looks it up.
	 *
	 * @param classInfo cached class info
	 * @param clazz the class to inspect
	 * @return class info
	 */
	private static ClassInfo getClassInfo(ClassInfo classInfo, Class<?> clazz) {
		return classInfo != null ? classInfo : ReflectionUtils.getClassInfo(clazz);
	}

	/**
	 * Returns all annotations found for the field.
	 *
	 * @return annotations keyed by type
	 */
	public Map<Class<? extends Annotation>, Annotation> getAnnotations() {
		return annotations;
	}

	/**
	 * Returns a specific annotation for the field.
	 *
	 * @param annotation the annotation type
	 * @return the annotation or null
	 */
	public Annotation getAnnotation(Class<? extends Annotation> annotation) {
		return annotations.get(annotation);
	}

	/**
	 * Checks whether an annotation exists for the field.
	 *
	 * @param annotation the annotation type
	 * @return true when present
	 */
	public boolean hasAnnotation(Class<? extends Annotation> annotation) {
		return annotations.containsKey(annotation);
	}

	/**
	 * Checks whether the field's type is considered a core type.
	 *
	 * @return true when the field type is a core type
	 */
	public boolean isCoreType() {
		return ReflectionUtils.isCoreType(field.getType());
	}

	/**
	 * Marks the field as accessible.
	 */
	@SuppressWarnings("java:S3011")
	public void makeAccessible() {
		field.setAccessible(true); // NOSNOAR: required for classes we don't control
	}

	/**
	 * Reads the field value, using a getter when present.
	 *
	 * @param o the instance to read from
	 * @return the field value
	 * @throws IllegalAccessException when access is denied
	 * @throws InvocationTargetException when accessor invocation fails
	 */
	@SuppressWarnings("unchecked")
	public T get(Object o) throws IllegalAccessException, InvocationTargetException {
		if (getter != null) {
			return (T) getter.invoke(o);
		}

		return (T) field.get(o);
	}

	/**
	 * Reads the field value, making the field accessible if needed.
	 *
	 * @param o the instance to read from
	 * @return the field value
	 * @throws IllegalAccessException when access is denied
	 * @throws InvocationTargetException when accessor invocation fails
	 */
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

	/**
	 * Sets the field value, using a setter when present.
	 *
	 * @param o the instance to modify
	 * @param value the value to set
	 * @throws InvocationTargetException when accessor invocation fails
	 * @throws IllegalAccessException when access is denied
	 */
	public void set(Object o, T value) throws InvocationTargetException, IllegalAccessException {
		if (setter != null) {
			setter.invoke(o, value);
		} else {
			field.set(o, value); // NOSONAR
		}
	}

	/**
	 * Forces the field value, making the field accessible if needed.
	 *
	 * @param o the instance to modify
	 * @param value the value to set
	 * @throws InvocationTargetException when accessor invocation fails
	 * @throws IllegalAccessException when access is denied
	 */
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
