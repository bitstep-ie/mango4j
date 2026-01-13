package ie.bitstep.mango.utils.masking;

import ie.bitstep.mango.utils.masking.CommaSeparatedIdListMasker;
import ie.bitstep.mango.utils.masking.Mask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommaSeparatedIdListMaskerTest {

	private final CommaSeparatedIdListMasker commaSeparatedIdListMasker = new CommaSeparatedIdListMasker();

	@Mask(maskingChars = "#")
	final String maskTestValue =  "TestingIdValue, u27;34p1-=+WQ[]/.><~`, 12344-221";

	@Test
	void maskCommaSeparatedValues() {
		String expectedResult = "TestiXXXXXXXXX, u27;XXXXXXXXXXXXXXXXX, 1234XXXXX";

		String result = commaSeparatedIdListMasker.mask(maskTestValue);

		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	void maskCommaSeparatedValuesWithCustomMaskCharacters() {
		String expectedResult = "Testi?????????, u27;?????????????????, 1234?????";

		String result = new CommaSeparatedIdListMasker("?").mask(maskTestValue);

		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	void maskCommaSeparatedValuesWithMaskAnnotation() throws NoSuchFieldException {
		String expectedResult = "Testi#########, u27;#################, 1234#####";
		Mask mask = this.getClass().getDeclaredField("maskTestValue").getAnnotation(Mask.class);

		String result = new CommaSeparatedIdListMasker(mask).mask(maskTestValue);

		assertThat(result).isEqualTo(expectedResult);
	}
}
