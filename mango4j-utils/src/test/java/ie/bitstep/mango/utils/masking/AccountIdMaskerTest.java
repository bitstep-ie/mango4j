package ie.bitstep.mango.utils.masking;

import ie.bitstep.mango.utils.masking.AccountIdMasker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by e031088 on 6/7/2017.
 */
class AccountIdMaskerTest {
	private final AccountIdMasker masker = new AccountIdMasker();

	@Test
	void testMask() {
		assertThat(masker.mask("01234567890ABCDEF")).isEqualTo("01234XXXXXXXXXXXX");
	}
}
