package ie.bitstep.mango.utils.masking;

import ie.bitstep.mango.utils.masking.AbstractCommaSeparatedListMasker;
import ie.bitstep.mango.utils.masking.Masker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractCommaSeparatedListMaskerTest {

	private final TestAbstractCommaSeparatedListMasker commaSeparatedListMasker = new TestAbstractCommaSeparatedListMasker(new TestMasker());

	private static final class TestAbstractCommaSeparatedListMasker extends AbstractCommaSeparatedListMasker {

		private TestAbstractCommaSeparatedListMasker(Masker masker) {
			super(masker);
		}
	}

	private static final class TestMasker implements Masker {
		private static final String TEST_MASKED_VALUE = "TestMaskedValue";

		@Override
		public String mask(String value) {
			return TEST_MASKED_VALUE;
		}
	}

	@Test
	void mask() {
		String result = commaSeparatedListMasker.mask("testValue1, testValue2, testValue3, testValue4");
		assertThat(result).isEqualTo(String.format("%1$s,%1$s,%1$s,%1$s", TestMasker.TEST_MASKED_VALUE));
	}
}
