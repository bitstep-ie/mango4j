package ie.bitstep.mango.utils.masking;

import ie.bitstep.mango.utils.masking.Mask;
import ie.bitstep.mango.utils.masking.Masker;
import ie.bitstep.mango.utils.masking.MaskerMaker;
import ie.bitstep.mango.utils.masking.NoMasker;
import ie.bitstep.mango.utils.masking.PanMasker;
import ie.bitstep.mango.utils.masking.ParameterisedMasker;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MaskerMakerTest {

	private final MaskerMaker maskerMaker = new MaskerMaker();
	@Mask(maskingChars = "~", prefix = 2, postfix = 2)
	public String s;

	@Mask(masker = NoMasker.class, maskingChars = "~", prefix = 2, postfix = 2)
	public String noMasker;

	@Mask(masker = PanMasker.class, maskingChars = "~", prefix = 2, postfix = 2)
	public String pan;

	private static final class MockedMaskerClassWhichThrowsInvocationTargetException implements Masker {
		public MockedMaskerClassWhichThrowsInvocationTargetException() throws InvocationTargetException {
			throw new InvocationTargetException() {
			};
		}

		public MockedMaskerClassWhichThrowsInvocationTargetException(
				String maskingCharacters // NOSONAR: required to match interface
		) throws InvocationTargetException {
			throw new InvocationTargetException() {
			};
		}

		@Override
		public String mask(String value) {
			return null;
		}
	}

	private static final class MockedMaskWhichReturnsExceptionThrowingMaskMethod implements Mask {
		public static final RuntimeException EXCEPTION = new RuntimeException("Test Masker Exception");

		public MockedMaskWhichReturnsExceptionThrowingMaskMethod() {
		}

		public MockedMaskWhichReturnsExceptionThrowingMaskMethod(Mask mask) {
			throw new RuntimeException("Test Constructor Exception");
		}

		@Override
		public Class<? extends Masker> masker() {
			throw EXCEPTION;
		}

		@Override
		public String maskingChars() {
			return "";
		}

		@Override
		public int prefix() {
			return 0;
		}

		@Override
		public int postfix() {
			return 0;
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return null;
		}
	}

	private static final class MockedMaskerClassWithoutSingleArgsConstructor implements Masker {

		public MockedMaskerClassWithoutSingleArgsConstructor() { // NOSONAR: test case support
		}

		@Override
		public String mask(String value) {
			return null;
		}
	}

	@Test
	void make() {
		assertThat(maskerMaker.make(PanMasker.class)).isExactlyInstanceOf(PanMasker.class);
	}

	@Test
	void makeException() {
		assertThatThrownBy(() -> maskerMaker.make(new MockedMaskWhichReturnsExceptionThrowingMaskMethod()))
				.isInstanceOf(IllegalStateException.class)
				.hasCause(MockedMaskWhichReturnsExceptionThrowingMaskMethod.EXCEPTION);
	}

	@Test
	void makeParameterised() throws NoSuchFieldException {
		Mask mask = this.getClass().getField("s").getAnnotation(Mask.class);
		Masker masker = maskerMaker.make(mask);

		assertThat(masker).isExactlyInstanceOf(ParameterisedMasker.class);
	}

	@Test
	void makeNotParameterised() throws NoSuchFieldException {
		Mask mask = this.getClass().getField("noMasker").getAnnotation(Mask.class);
		Masker masker = maskerMaker.make(mask);

		assertThat(masker).isExactlyInstanceOf(NoMasker.class);
	}

	@Test
	void makeNonParameterised() throws NoSuchFieldException {
		Mask mask = this.getClass().getField("pan").getAnnotation(Mask.class);

		Masker masker = maskerMaker.make(mask);

		assertThat(masker).isExactlyInstanceOf(PanMasker.class);
	}

	@Test
	void makeWithCustomMaskingCharacters() throws NoSuchFieldException, IllegalAccessException {
		String customMaskingCharacters = "custom";

		Masker masker = maskerMaker.make(PanMasker.class, customMaskingCharacters);

		assertThat(masker).isExactlyInstanceOf(PanMasker.class);

		Field f = getField(masker.getClass(), "maskingCharacters");
		f.setAccessible(true);
		Object o = f.get(masker);

		assertThat(o).isEqualTo(customMaskingCharacters);
	}

	private static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			} else {
				return getField(superClass, fieldName);
			}
		}
	}

	@Test
	void createMaskerInvocationTargetException() {
		assertThatThrownBy(() -> maskerMaker.make(MockedMaskerClassWhichThrowsInvocationTargetException.class))
				.isInstanceOf(IllegalStateException.class)
				.hasCauseInstanceOf(InvocationTargetException.class);
	}

	@Test
	void makeWithCustomMaskingCharactersUnsupportedConstructor() {
		String customMaskingCharacters = "custom";

		Masker masker = maskerMaker.make(MockedMaskerClassWithoutSingleArgsConstructor.class, customMaskingCharacters);

		assertThat(masker).isExactlyInstanceOf(MockedMaskerClassWithoutSingleArgsConstructor.class);
	}

	@Test
	void makeWithCustomMaskingCharactersInvocationTargetException() {
		assertThatThrownBy(() -> maskerMaker.make(MockedMaskerClassWhichThrowsInvocationTargetException.class, "customMaskingCharacters"))
				.isInstanceOf(IllegalStateException.class)
				.hasCauseInstanceOf(InvocationTargetException.class);
	}
}
