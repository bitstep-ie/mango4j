package ie.bitstep.mango.utils.converters;

import ie.bitstep.mango.utils.converters.CollectionToSize;
import ie.bitstep.mango.utils.converters.ConverterFactory;
import ie.bitstep.mango.utils.converters.NoConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

class ConverterFactoryTest {

	private static class BadConverter implements UnaryOperator<Object> {
		public Object apply(Object o) {
			return null;
		}
	}

	@Test
	void convert() {
		String s1 = UUID.randomUUID().toString();
		String s2 = (String) ConverterFactory.convert(NoConverter.class, s1);
		assertThat(s1).isInstanceOf(s2.getClass());
	}

	@Test
	void getConverter_whenFirst() {
		UnaryOperator<Object> converter = ConverterFactory.getConverter(CollectionToSize.class);
		assertThat(converter).isNotNull().isInstanceOf(CollectionToSize.class);
	}

	@Test
	void getConverter_whenRepeated() {
		UnaryOperator<Object> converter1 = ConverterFactory.getConverter(CollectionToSize.class);
		UnaryOperator<Object> converter2 = ConverterFactory.getConverter(CollectionToSize.class);

		assertThat(converter1).isNotNull().isInstanceOf(CollectionToSize.class);
		assertThat(converter2).isNotNull().isInstanceOf(CollectionToSize.class);
	}

	@Test
	void createConverter() {
		UnaryOperator<Object> converter = ConverterFactory.createConverter(CollectionToSize.class);
		assertThat(converter).isNotNull().isInstanceOf(CollectionToSize.class);
	}

	@Test
	void getConverter_whenException() {
		IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, () -> ConverterFactory.getConverter(BadConverter.class), "ie.bitstep.mango.utils.converter.ConverterFactoryTest$IllegalConverter.<init>()");

		assertThat(thrown).isNotNull(); // lazy man's check
		assertThat(thrown.getMessage()).isEqualTo("java.lang.NoSuchMethodException: ie.bitstep.mango.utils.converters.ConverterFactoryTest$BadConverter.<init>()");
	}

	@Test
	void privateConstructor() throws Exception {
		Constructor<ConverterFactory> constructor = ConverterFactory.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		assertThat(constructor.newInstance()).isInstanceOf(ConverterFactory.class);
	}

}