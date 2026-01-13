package ie.bitstep.mango.utils.converters;

import ie.bitstep.mango.utils.converters.CollectionToSize;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


class CollectionToSizeTest {

	private final CollectionToSize converter = new CollectionToSize();

	@Test
	void apply_whenNull() {
		assertThat(converter.apply(null)).isNull();
	}

	@Test
	void apply_whenEmpty() {
		assertThat(converter.apply(Collections.emptyList())).isZero();
	}

	@Test
	void apply_whenList() {
		assertThat(converter.apply(Arrays.asList(11, 22, 33))).isEqualTo(3);
	}
}