package ie.bitstep.mango.utils.masking;

import ie.bitstep.mango.utils.masking.PanMasker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PanMaskerTest {

	private final PanMasker masker = new PanMasker();

	@Test
	void maskPanLongerThanAffixesTotalLength() {
		assertThat(masker.mask("01234567890123456")).isEqualTo("012345XXXXXXX3456");
	}

	@Test
	void maskPanSameSizeAsAffixesTotalLength() {
		assertThat(masker.mask("0123456789")).isEqualTo("XXXXXXXXXX");
	}

	@Test
	void maskPanShorterThanAffixesTotalLength() {
		assertThat(masker.mask("012345678")).isEqualTo("XXXXXXXXX");
	}

	@Test
	void maskNullPan() {
		assertThat(masker.mask(null)).isEmpty();
	}

	@Test
	void maskEmptyPan() {
		assertThat(masker.mask("")).isEmpty();
	}

	@Test
	void maskPanWithCustomCharacters() {
		assertThat(new PanMasker("Y").mask("01234567890123456")).isEqualTo("012345YYYYYYY3456");
	}
}
