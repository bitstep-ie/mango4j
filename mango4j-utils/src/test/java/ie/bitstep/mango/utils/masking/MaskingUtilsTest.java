package ie.bitstep.mango.utils.masking;

import ie.bitstep.mango.utils.masking.MaskingUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static ie.bitstep.mango.utils.masking.MaskingUtils.MINIMUM_LENGTH_OF_MASKING_AFFIXES;
import static ie.bitstep.mango.utils.masking.MaskingUtils.mask;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaskingUtilsTest {

	@Test
	void privateConstructor() throws Exception {
		Constructor<MaskingUtils> constructor = MaskingUtils.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		assertThat(constructor.newInstance()).isInstanceOf(MaskingUtils.class);
	}

	@Test
	void maskPrefixLengthLessThan1() {
		int maskPrefixLength = -1;
		int maskSuffixLength = 1;
		assertThatThrownBy(() -> mask("testValue", "X", maskPrefixLength, maskSuffixLength))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("maskPrefixLength and maskSuffixLength must be greater than 0.Supplied values were %s and %s respectively", maskPrefixLength, maskSuffixLength);
	}

	@Test
	void maskSuffixLengthLessThan1() {
		int maskPrefixLength = 1;
		int maskSuffixLength = -1;
		assertThatThrownBy(() -> mask("testValue", "X", maskPrefixLength, maskSuffixLength))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("maskPrefixLength and maskSuffixLength must be greater than 0.Supplied values were %s and %s respectively", maskPrefixLength, maskSuffixLength);
	}

	@Test
	void maskAffixesLengthsBothLessThan1() {
		int maskPrefixLength = -11;
		int maskSuffixLength = -1;
		assertThatThrownBy(() -> mask("testValue", "X", maskPrefixLength, maskSuffixLength))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("maskPrefixLength and maskSuffixLength must be greater than 0.Supplied values were %s and %s respectively", maskPrefixLength, maskSuffixLength);
	}

	@Test
	void maskPrefixLengthExactMinimum() {
		int maskPrefixLength = MINIMUM_LENGTH_OF_MASKING_AFFIXES;
		int maskSuffixLength = MINIMUM_LENGTH_OF_MASKING_AFFIXES;
		assertThat(mask("testValue", "X", maskPrefixLength, maskSuffixLength)).isEqualTo("XXXXXXXXX");
	}

	@Test
	void maskNullValue() {
		assertThat(mask(null, "X", 5, 5)).isEmpty();
	}

	@Test
	void maskEmptyValue() {
		assertThat(mask("", "X", 5, 5)).isEmpty();
	}

	@Test
	void maskValueLessThatTotalAffixesLengths() {
		assertThat(mask("test", "X", 2, 2)).isEqualTo("XXXX");
	}

	@Test
	void maskValueEqualToTotalAffixesLengths() {
		assertThat(mask("testValue", "X", 5, 4)).isEqualTo("XXXXXXXXX");
	}

	@Test
	void maskValueGreaterThanTotalAffixesLengths() {
		assertThat(mask("testValue", "X", 5, 3)).isEqualTo("testVXlue");
	}

	@Test
	void maskValueMuchGreaterThanTotalAffixesLengths() {
		assertThat(mask("testValueThatsPrettyLong", "X", 6, 4)).isEqualTo("testVaXXXXXXXXXXXXXXLong");
	}
}
