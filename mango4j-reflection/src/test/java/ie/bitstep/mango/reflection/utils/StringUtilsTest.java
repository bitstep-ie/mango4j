package ie.bitstep.mango.reflection.utils;

import ie.bitstep.mango.reflection.utils.StringUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilsTest {

	@Test
	void capitalize() {
		assertThat(StringUtils.capitalize("hello")).isEqualTo("Hello");
	}

	@Test
	void capitalizeBlank() {
		assertThat(StringUtils.capitalize("")).isEmpty();
	}

	@Test
	void capitalizeNull() {
		assertThat(StringUtils.capitalize(null)).isNull();
	}
}