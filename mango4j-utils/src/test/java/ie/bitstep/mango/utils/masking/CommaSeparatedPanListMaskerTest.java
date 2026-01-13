package ie.bitstep.mango.utils.masking;

import ie.bitstep.mango.utils.masking.CommaSeparatedPanListMasker;
import ie.bitstep.mango.utils.masking.Mask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommaSeparatedPanListMaskerTest {

	private final CommaSeparatedPanListMasker commaSeparatedPanListMasker = new CommaSeparatedPanListMasker();

	@Mask(maskingChars = "#")
	final String values = "5404361417512107, 540436843235015858, 54043615067258";

	@Test
	void maskCommaSeparatedPan() {
		String expectedResult = "540436XXXXXX2107, 54043XXXXXXXXX5858, 54043XXXXX7258";

		String result = commaSeparatedPanListMasker.mask(values);

		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	void maskCommaSeparatedPanWithCustomMaskCharacters() {
		String expectedResult = "540436??????2107, 54043?????????5858, 54043?????7258";

		String result = new CommaSeparatedPanListMasker("?").mask(values);

		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	void maskCommaSeparatedValuesWithMaskAnnotation() throws NoSuchFieldException {
		String expectedResult = "540436######2107, 54043#########5858, 54043#####7258";
		Mask mask = this.getClass().getDeclaredField("values").getAnnotation(Mask.class);

		String result = new CommaSeparatedPanListMasker(mask).mask(values);

		assertThat(result).isEqualTo(expectedResult);
	}

}
