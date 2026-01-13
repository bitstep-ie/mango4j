package ie.bitstep.mango.reflection.utils.test;

import ie.bitstep.mango.reflection.utils.test.MDCTestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class MDCTestUtilsTest {

	@Test
	void init() {
		// GIVEN

		// WHEN
		MDCTestUtils.init();
		MDC.put("Key", "Value");

		// THEN
		assertThat(MDCTestUtils.getMDCAdapter()).isNotNull();
		assertThat(MDC.get("Key")).isEqualTo("Value");
	}

	@Test
	void cleanup() {
		// GIVEN

		// WHEN
		MDCTestUtils.init();
		MDC.put("Key", "Value");

		MDCTestUtils.cleanup();

		// THEN
		assertThat(MDCTestUtils.getMDCAdapter()).isNull();
		assertThat(MDC.get("Key")).isNotEqualTo("Value");
	}
}