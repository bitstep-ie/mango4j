package ie.bitstep.mango.utils.masking;

import ie.bitstep.mango.utils.masking.IdMasker;
import ie.bitstep.mango.utils.masking.Mask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdMaskerTest {

	private final IdMasker masker = new IdMasker();

	@Mask(maskingChars = "%")
	final String id = "63e2ffef-c655-45af-ae20-b0e7b29ce3da";

	@Test
	void maskValueLongerThanAffixesTotalLength() {
		assertThat(masker.mask("01234567890ABCDEF")).isEqualTo("01234XXXXXXXXXXXX");
	}

	@Test
	void maskValueSameSizeAsAffixesTotalLength() {
		assertThat(masker.mask("ABCDE")).isEqualTo("XXXXX");
	}

	@Test
	void maskValueShorterThanAffixesTotalLength() {
		assertThat(masker.mask("ABCD")).isEqualTo("XXXX");
	}

	@Test
	void maskNullValue() {
		assertThat(masker.mask(null)).isEmpty();
	}

	@Test
	void maskEmptyValue() {
		assertThat(masker.mask("")).isEmpty();
	}

	@Test
	void maskValueWithCustomCharacters() {
		assertThat(new IdMasker("Y").mask("01234567890ABCDEF")).isEqualTo("01234YYYYYYYYYYYY");
	}

	@Test
	void mask() throws NoSuchFieldException {
		String expectedResult = "63e2f%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%";
		Mask mask = this.getClass().getDeclaredField("id").getAnnotation(Mask.class);

		String result = new IdMasker(mask).mask(id);

		assertThat(result).isEqualTo(expectedResult);
	}
}
