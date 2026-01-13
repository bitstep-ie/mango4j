package ie.bitstep.mango.utils.masking;

import ie.bitstep.mango.utils.masking.CommaSeparatedIdListMasker;
import ie.bitstep.mango.utils.masking.Mask;
import ie.bitstep.mango.utils.masking.Masker;
import ie.bitstep.mango.utils.masking.MaskerFactory;
import ie.bitstep.mango.utils.masking.MaskerMaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MaskerFactoryTest {
	@Mock
	private MaskerMaker mockMaskerMaker;

	private TestMasker testMasker;
	private TestMasker testMaskerWithCustomCharacters;

	@Mask(maskingChars = "#", prefix = 2, postfix = 2)
	public String s;

	private static final class TestMasker implements Masker {
		@Override
		public String mask(String value) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() throws NoSuchFieldException, IllegalAccessException {
		Field f = MaskerFactory.class.getDeclaredField("maskerMaker");
		f.setAccessible(true);
		f.set(null, mockMaskerMaker);

		f = MaskerFactory.class.getDeclaredField("DEFAULT_MASKERS");
		f.setAccessible(true);
		Map<Class<? extends Masker>, Masker> map = (Map<Class<? extends Masker>, Masker>) f.get(null);
		map.clear();

		f = MaskerFactory.class.getDeclaredField("MASKERS_WITH_CUSTOM_MASKING_CHARACTERS");
		f.setAccessible(true);
		map = (Map<Class<? extends Masker>, Masker>) f.get(null);
		map.clear();

		testMasker = new TestMasker();
		testMaskerWithCustomCharacters = new TestMasker();
	}

	@Test
	void constructor() throws Exception {
		Constructor<MaskerFactory> constructor = MaskerFactory.class.getDeclaredConstructor();
		constructor.setAccessible(true);

		assertThat(constructor.newInstance()).isInstanceOf(MaskerFactory.class);
	}

	@Test
	void tupleHashCode() {
		MaskerFactory.Tuple tuple = new MaskerFactory.Tuple(TestMasker.class, "customMaskingCharacters");

		assertThat(tuple.hashCode()).isNotZero();
	}

	@Test
	void getMasker() {
		given(mockMaskerMaker.make(TestMasker.class)).willReturn(testMasker);

		assertThat(MaskerFactory.getMasker(TestMasker.class)).isSameAs(testMasker);

		then(mockMaskerMaker).should().make(TestMasker.class);
		then(mockMaskerMaker).shouldHaveNoMoreInteractions();
	}

	@Test
	void getMaskerInstanceAlreadyExists() {
		given(mockMaskerMaker.make(TestMasker.class)).willReturn(testMasker);

		MaskerFactory.getMasker(TestMasker.class);
		assertThat(MaskerFactory.getMasker(TestMasker.class)).isSameAs(testMasker);

		then(mockMaskerMaker).should().make(TestMasker.class);
		then(mockMaskerMaker).shouldHaveNoMoreInteractions();
	}

	@Test
	void getMaskerByAnnotation() throws NoSuchFieldException {
		Mask mask = this.getClass().getField("s").getAnnotation(Mask.class);
		given(mockMaskerMaker.make(mask)).willReturn(testMasker);

		MaskerFactory.getMasker(mask);
		assertThat(MaskerFactory.getMasker(mask)).isSameAs(testMasker);

		then(mockMaskerMaker).should().make(mask);
		then(mockMaskerMaker).shouldHaveNoMoreInteractions();
	}

	@Test
	void getMaskerWithCustomCharacters() {
		String customMaskingCharacters = "customMaskingCharacters";
		given(mockMaskerMaker.make(TestMasker.class, customMaskingCharacters)).willReturn(testMaskerWithCustomCharacters);

		assertThat(MaskerFactory.getMasker(TestMasker.class, customMaskingCharacters)).isSameAs(testMaskerWithCustomCharacters);

		then(mockMaskerMaker).should().make(TestMasker.class, customMaskingCharacters);
		then(mockMaskerMaker).shouldHaveNoMoreInteractions();
	}

	@Test
	void getMaskerWithCustomCharactersInstanceAlreadyExists() {
		String customMaskingCharacters = "customMaskingCharacters";
		given(mockMaskerMaker.make(TestMasker.class, customMaskingCharacters)).willReturn(testMaskerWithCustomCharacters);

		MaskerFactory.getMasker(TestMasker.class, customMaskingCharacters);
		assertThat(MaskerFactory.getMasker(TestMasker.class, customMaskingCharacters)).isSameAs(testMaskerWithCustomCharacters);

		then(mockMaskerMaker).should().make(TestMasker.class, customMaskingCharacters);
		then(mockMaskerMaker).shouldHaveNoMoreInteractions();
	}

	@Test
	void getMaskerWithCustomCharactersEmpty() {
		String customMaskingCharacters = "";
		given(mockMaskerMaker.make(TestMasker.class)).willReturn(testMaskerWithCustomCharacters);

		assertThat(MaskerFactory.getMasker(TestMasker.class, customMaskingCharacters)).isSameAs(testMaskerWithCustomCharacters);

		then(mockMaskerMaker).should().make(TestMasker.class);
		then(mockMaskerMaker).shouldHaveNoMoreInteractions();
	}

	@Test
	void tupleEqualityTest() {
		MaskerFactory.Tuple tuple = new MaskerFactory.Tuple(Masker.class, "name");
		MaskerFactory.Tuple differentClass = new MaskerFactory.Tuple(CommaSeparatedIdListMasker.class, "name");
		MaskerFactory.Tuple differentName = new MaskerFactory.Tuple(Masker.class, "another-name");
		MaskerFactory.Tuple nullTuple = null;
		String nullString = null;
		assertThat(tuple).isEqualTo(tuple);
		assertThat(nullTuple).isNotEqualTo(tuple);
		assertThat(tuple).isNotEqualTo(nullTuple);
		assertThat(differentName).isNotEqualTo(tuple);
		assertThat(differentClass).isNotEqualTo(tuple);
		assertThat(tuple)
				.isNotEqualTo("new String()")
				.isNotEqualTo(nullString);
	}

}
