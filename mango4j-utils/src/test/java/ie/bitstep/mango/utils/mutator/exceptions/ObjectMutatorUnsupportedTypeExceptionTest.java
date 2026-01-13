package ie.bitstep.mango.utils.mutator.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectMutatorUnsupportedTypeExceptionTest {
	public UUID id = UUID.randomUUID();
	public byte[] ba = new byte[]{0x01};
	public String[] sa = new String[]{""};

	@Test
	void testExceptionWithMessage() {
		String msg = "test message";
		MutatorUnsupportedTypeException mutatorUnsupportedTypeException = new MutatorUnsupportedTypeException(msg);

		assertThat(mutatorUnsupportedTypeException.getMessage()).isEqualTo(msg);
	}

	@ParameterizedTest
	@CsvSource(value = {
			"id;id, unsupported type java.util.UUID",
			"ba;ba, unsupported type byte[]",
			"sa;sa, unsupported type java.lang.String[]",
	}, delimiter = ';')
	void testExceptionWithField(String fieldName, String expectedErrorMessage) throws NoSuchFieldException {
		Field field = ObjectMutatorUnsupportedTypeExceptionTest.class.getField(fieldName);
		MutatorUnsupportedTypeException mutatorUnsupportedTypeException = new MutatorUnsupportedTypeException(field);

		assertThat(mutatorUnsupportedTypeException.getMessage()).isEqualTo(expectedErrorMessage);
	}
}
