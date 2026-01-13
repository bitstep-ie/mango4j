package ie.bitstep.mango.utils.masking;

import ie.bitstep.mango.utils.masking.NoMasker;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NoMaskerTest {

	private final String testValue = UUID.randomUUID().toString();

	@Test
	void mask() {
		assertThat(new NoMasker().mask(testValue)).isEqualTo(testValue);
	}
}
