package ie.bitstep.mango.utils.masking;

import ie.bitstep.mango.utils.masking.FullMasker;
import ie.bitstep.mango.utils.masking.Mask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FullMaskerTest {

	private final FullMasker masker = new FullMasker();

	@Mask(maskingChars = "<<>>")
	final String value = "63e2ffef-c655-45af-ae20-b0e7b29ce3da";

	@Test
	void maskNull() {
		assertThat(masker.mask(null)).isNull();
	}

	@Test
	void maskEmpty() {
		assertThat(masker.mask("")).isEmpty();
	}

	@Test
	void maskValueLessThanMaskLength() {
		assertThat(masker.mask("A")).isEqualTo("***");
	}

	@Test
	void maskValueEqualToMaskLength() {
		assertThat(masker.mask("ABC")).isEqualTo("***");
	}

	@Test
	void maskValueGreaterThanMaskLength() {
		assertThat(masker.mask("ABCDEFDFWGHDFGDFSGHSDGHDFGSDFHF")).isEqualTo("***");
	}

	@Test
	void maskWithCustomMaskCharacters() {
		assertThat(new FullMasker("???").mask("ABCDEFDFWGHDFGDFSGHSDGHDFGSDFHF")).isEqualTo("???");
	}

	@Test
	void mask() throws NoSuchFieldException {
		String expectedResult = "<<>>";
		Mask mask = this.getClass().getDeclaredField("value").getAnnotation(Mask.class);

		String result = new FullMasker(mask).mask(value);

		assertThat(result).isEqualTo(expectedResult);
	}

}
