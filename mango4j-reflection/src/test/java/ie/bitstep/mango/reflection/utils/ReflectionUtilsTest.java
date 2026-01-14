package ie.bitstep.mango.reflection.utils;

import ie.bitstep.mango.reflection.annotations.Modifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ReflectionUtilsTest {
	NoAccessorsPublicField noAccessorsPublicField = new NoAccessorsPublicField();

	@Test
	void isCoreType() {
		UUID uuid = UUID.randomUUID();

		assertThat(ReflectionUtils.isCoreType(Integer.class)).isTrue();
		assertThat(ReflectionUtils.isCoreType(String.class)).isTrue();
		assertThat(ReflectionUtils.isCoreType(Boolean.class)).isTrue();
		assertThat(ReflectionUtils.isCoreType(Long.class)).isTrue();
		assertThat(ReflectionUtils.isCoreType(Integer.class)).isTrue();
		assertThat(ReflectionUtils.isCoreType(Float.class)).isTrue();
		assertThat(ReflectionUtils.isCoreType(Double.class)).isTrue();
		assertThat(ReflectionUtils.isCoreType(uuid.getClass())).isFalse();
		assertThat(ReflectionUtils.isCoreType(uuid)).isFalse();
		assertThat(ReflectionUtils.isCoreType(1)).isTrue();
	}

	@Test
	void getClassInfoClass() {
		ClassInfo ci = ReflectionUtils.getClassInfo(noAccessorsPublicField.getClass());

		assertThat(ci.getMethod("dummy")).isNotNull();
	}

	@Test
	void getClassInfoObject() {
		ClassInfo ci = ReflectionUtils.getClassInfo(noAccessorsPublicField);

		assertThat(ci.getMethod("dummy")).isNotNull();
	}

	@Test
	void setFieldByName() throws NoSuchFieldException, InvocationTargetException, IllegalAccessException {
		ReflectionUtils.setField(noAccessorsPublicField, "s", "Hello");

		assertThat(noAccessorsPublicField.s).isEqualTo("Hello");
	}

	@Test
	void forceSetFieldByName() throws NoSuchFieldException, InvocationTargetException, IllegalAccessException {
		// GIVEN
		String tmp = UUID.randomUUID().toString();
		ReflectionUtils.forceSetField(noAccessorsPublicField, "s", tmp);

		assertThat(ReflectionUtils.forceGetField(noAccessorsPublicField, "s")).isEqualTo(tmp);
	}

	@Test
	void forceSetField() throws NoSuchFieldException, InvocationTargetException, IllegalAccessException {
		// GIVEN
		Field f = ReflectionUtils.getClassInfo(NoAccessorsPublicField.class).getClazz().getField("s");
		String tmp = UUID.randomUUID().toString();

		// WHEN
		ReflectionUtils.forceSetField(noAccessorsPublicField, f, tmp);

		// THEN
		assertThat(ReflectionUtils.forceGetField(noAccessorsPublicField, f)).isEqualTo(tmp);
	}

	@Test
	void setField() throws NoSuchFieldException, InvocationTargetException, IllegalAccessException {
		Field field = noAccessorsPublicField.getClass().getDeclaredField("s");
		ReflectionUtils.setField(noAccessorsPublicField, field, "Hello");

		assertThat(noAccessorsPublicField.s).isEqualTo("Hello");
	}

	@Test
	void classField() throws NoSuchFieldException, InvocationTargetException, IllegalAccessException {
		// GIVEN
		String tmp = UUID.randomUUID().toString();
		ReflectionUtils.setField(noAccessorsPublicField.getClass(), "staticS", tmp);

		assertThat(ReflectionUtils.getField(noAccessorsPublicField.getClass(), "staticS")).isEqualTo(tmp);
	}

	@Test
	void forceClassField() throws NoSuchFieldException, InvocationTargetException, IllegalAccessException {
		// GIVEN
		String tmp = UUID.randomUUID().toString();
		ReflectionUtils.forceSetField(noAccessorsPublicField.getClass(), "staticS", tmp);

		assertThat(ReflectionUtils.forceGetField(noAccessorsPublicField.getClass(), "staticS")).isEqualTo(tmp);
	}

	@Test
	void getFieldByName() throws NoSuchFieldException, InvocationTargetException, IllegalAccessException {
		ReflectionUtils.setField(noAccessorsPublicField, "s", "Goodbye");

		assertThat(ReflectionUtils.getField(noAccessorsPublicField, "s")).isEqualTo("Goodbye");
	}

	@Test
	void getField() throws NoSuchFieldException, InvocationTargetException, IllegalAccessException {
		Field field = noAccessorsPublicField.getClass().getDeclaredField("s");
		ReflectionUtils.setField(noAccessorsPublicField, field, "Goodbye");

		assertThat(ReflectionUtils.getField(noAccessorsPublicField, field)).isEqualTo("Goodbye");
	}

	@Test
	void getMethodNoArgs() {
		Method m = ReflectionUtils.getMethod(noAccessorsPublicField.getClass(), "dummy");

		assertThat(m).isNotNull();
		assertThat(m.getName()).isEqualTo("dummy");
	}

	@Test
	void getMethodNoStringArg() {
		Method m = ReflectionUtils.getMethod(noAccessorsPublicField.getClass(), "message", String.class);

		assertThat(m).isNotNull();
		assertThat(m.getName()).isEqualTo("message");
	}

	@Test
	void getMethodInfoByAnnotation() {
		List<MethodInfo> m = ReflectionUtils.getMethodInfoByAnnotation(noAccessorsPublicField.getClass(), Modifier.class);

		assertThat(m).hasSize(1);
		assertThat(m.get(0).getMethod().getName()).isEqualTo("dummy");
	}

	@Test
	void getMethodInfoByName() {
		List<MethodInfo> m = ReflectionUtils.getMethodInfoByName(noAccessorsPublicField.getClass(), "dummy");

		assertThat(m).hasSize(1);
		assertThat(m.get(0).getMethod().getName()).isEqualTo("dummy");
	}
}