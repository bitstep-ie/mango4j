package ie.bitstep.mango.reflection.utils;

import ie.bitstep.mango.reflection.accessors.PropertyGetter;
import ie.bitstep.mango.reflection.utils.ClassInfo;
import ie.bitstep.mango.reflection.utils.MethodInfo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClassInfoTest {

	private String name = "Hello";

	@PropertyGetter("test")
	public void testMethod() {

	}

	@PropertyGetter("test")
	public void testMethod(String s) {

	}

	@PropertyGetter("test")
	public void testMethodWithArgs(String s, Integer i) {

	}

	@Test
	void getClassInfo() {
		ClassInfo classInfo = new ClassInfo(this.getClass());

		assertThat(classInfo.getClazz()).isEqualTo(this.getClass());
		assertThat(classInfo.getPropertyAccessors()).isNotNull();
		assertThat(classInfo.getPropertyAccessors().stream().findFirst().get().getGetter()).isNull();
		assertThat(classInfo.getPropertyAccessors().stream().findFirst().get().getSetter()).isNull();
	}

	@Test
	void testEquals() {
		ClassInfo classInfo1 = new ClassInfo(this.getClass());
		ClassInfo classInfo2 = new ClassInfo(this.getClass());

		assertThat(classInfo1)
			.isEqualTo(classInfo1)
			.isEqualTo(classInfo2)
			.isNotEqualTo(null)
			.isNotEqualTo(new ClassInfo(String.class));
	}

	@Test
	void testHashCode() {
		ClassInfo classInfo1 = new ClassInfo(this.getClass());
		ClassInfo classInfo2 = new ClassInfo(this.getClass());

		assertThat(classInfo1.hashCode())
			.isNotZero()
			.hasSameHashCodeAs(classInfo2);
	}

	@Test
	void propertyAccessInitialised() {
		ClassInfo classInfo = new ClassInfo(this.getClass());

		assertThat(classInfo.getPropertyAccessor("name")).isNotNull();
		assertThat(classInfo.getPropertyAccessor("name").getFieldName()).isNotNull();
	}

	@Test
	void getMethodByName() throws NoSuchMethodException {
		ClassInfo classInfo = new ClassInfo(this.getClass());
		Method method = classInfo.getMethod("testMethod");
		Method expected = this.getClass().getMethod("testMethod");

		assertThat(classInfo.getPropertyAccessor("name")).isNotNull();
		assertThat(classInfo.getPropertyAccessor("name").getFieldName()).isNotNull();
		assertThat(method).isEqualTo(expected);
	}

	@Test
	void getMethodByNameHasParams() {
		ClassInfo classInfo = new ClassInfo(this.getClass());
		Method method = classInfo.getMethod("testMethodWithArgs");

		assertThat(method).isNull();
	}

	@Test
	void getMethodByNameDoesNotExist() throws NoSuchMethodException {
		ClassInfo classInfo = new ClassInfo(this.getClass());
		Method method = classInfo.getMethod("testMethodX");

		assertThat(method).isNull();
	}

	@Test
	void getMethodByNameWithParams() throws NoSuchMethodException {
		ClassInfo classInfo = new ClassInfo(this.getClass());
		Method method = classInfo.getMethod("testMethodWithArgs", String.class, Integer.class);

		assertThat(method).isEqualTo(this.getClass().getMethod("testMethodWithArgs", String.class, Integer.class));
	}

	@Test
	void getMethodByNameWithParamsWrongParamTypes() throws NoSuchMethodException {
		ClassInfo classInfo = new ClassInfo(this.getClass());
		Method method = classInfo.getMethod("testMethod", Integer.class);

		assertThat(method).isNull();
	}

	@Test
	void getMethodInfoByAnnotation() throws NoSuchMethodException {
		ClassInfo classInfo = new ClassInfo(this.getClass());
		List<MethodInfo> methods = classInfo.getMethodInfoByAnnotation(PropertyGetter.class);

		assertThat(methods).contains(new MethodInfo(this.getClass().getMethod("testMethod")), new MethodInfo(this.getClass().getMethod("testMethod", String.class)));
	}

	@Test
	void getMethodInfoByName() throws NoSuchMethodException {
		ClassInfo classInfo = new ClassInfo(this.getClass());
		List<MethodInfo> methods = classInfo.getMethodInfoByName("testMethod");

		assertThat(methods).contains(new MethodInfo(this.getClass().getMethod("testMethod")), new MethodInfo(this.getClass().getMethod("testMethod", String.class)));
	}

	@Test
	void getMethodInfoByNameDoesNotExist() throws NoSuchMethodException {
		ClassInfo classInfo = new ClassInfo(this.getClass());
		List<MethodInfo> methods = classInfo.getMethodInfoByName("testMethodX");

		assertThat(methods).isEmpty();
	}
}