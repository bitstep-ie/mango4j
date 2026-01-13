package ie.bitstep.mango.utils.converters;

import ie.bitstep.mango.utils.converters.NoConverter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class NoConverterTest {

	private final NoConverter converter = new NoConverter();

	@Test
	void apply() {
		assertThat(converter.apply("55")).isEqualTo("55");
	}

	@Test
	void apply_whenNull() {
		assertThat(converter.apply(null)).isNull();
	}
}