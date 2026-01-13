package ie.bitstep.mango.utils.masking;

import ie.bitstep.mango.utils.masking.Mask;
import ie.bitstep.mango.utils.masking.ParameterisedMasker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterisedMaskerTest {
	@Mask(maskingChars = "!", prefix = 2, postfix = 2)
	public String s;

	@Test
	void mask() throws NoSuchFieldException {
		Mask mask = this.getClass().getField("s").getAnnotation(Mask.class);
		ParameterisedMasker parameterisedMasker = new ParameterisedMasker(mask);
		String result = parameterisedMasker.mask("testValue");

		assertThat(result).isEqualTo("te!!!!!ue");
	}
}
