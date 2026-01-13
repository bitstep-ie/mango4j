package ie.bitstep.mango.reflection.utils;

import ie.bitstep.mango.reflection.annotations.Mask;
import ie.bitstep.mango.reflection.annotations.Modifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MethodInfoTest {

	public void method1() {

	}

	public void method2() {

	}

	public void parameterAnnotationsMethod(@Modifier String s) {

	}

	@Test
	void testGetParameterAnnotations() throws NoSuchMethodException {
		MethodInfo methodInfo = new MethodInfo(this.getClass().getMethod("parameterAnnotationsMethod", String.class));
		Exception thrown;

		assertThat(methodInfo.getParameterAnnotations()).hasSize(1);
		assertThat(methodInfo.getParameterAnnotations(0)).containsKey(Modifier.class);
		assertThat(methodInfo.findParameterAnnotation(0, Modifier.class)).isPresent();
		assertThat(methodInfo.hasParameterAnnotation(0, Modifier.class)).isTrue();
		assertThat(methodInfo.findParameterAnnotation(0, Mask.class)).isNotPresent();
		assertThat(methodInfo.hasParameterAnnotation(0, Mask.class)).isFalse();

		thrown = assertThrows(IndexOutOfBoundsException.class, () ->
			methodInfo.getParameterAnnotations(1)
		);

		assertThat(thrown.getMessage()).isEqualTo("Index 1 out of bounds for length 1");

		assertThat(methodInfo.getParameterAnnotation(0, Modifier.class)).isNotNull();

		thrown = assertThrows(IllegalArgumentException.class, () ->
			methodInfo.getParameterAnnotation(0, Mask.class)
		);

		assertThat(thrown.getMessage()).isEqualTo("Annotations not found: ie.bitstep.mango.reflection.annotations.Mask");

		thrown = assertThrows(IndexOutOfBoundsException.class, () ->
			methodInfo.findParameterAnnotation(1, Modifier.class)
		);

		assertThat(thrown.getMessage()).isEqualTo("Index 1 out of bounds for length 1");
	}

	@Test
	void testEquals() throws NoSuchMethodException {
		MethodInfo methodInfo1 = new MethodInfo(this.getClass().getMethod("method1"));
		MethodInfo methodInfo2 = new MethodInfo(this.getClass().getMethod("method1"));

		assertThat(methodInfo1)
			.isEqualTo(methodInfo1)
			.isEqualTo(methodInfo2)
			.isNotEqualTo(null)
			.isNotEqualTo(new MethodInfo(this.getClass().getMethod("method2")));
	}

	@Test
	void testHashCode() throws NoSuchMethodException {
		MethodInfo methodInfo1 = new MethodInfo(this.getClass().getMethod("method1"));
		MethodInfo methodInfo2 = new MethodInfo(this.getClass().getMethod("method2"));

		assertThat(methodInfo1.hashCode())
			.isNotZero()
			.isNotSameAs(methodInfo2.hashCode());
	}
}