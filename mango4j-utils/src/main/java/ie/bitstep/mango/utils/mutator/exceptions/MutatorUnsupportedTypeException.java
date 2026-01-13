package ie.bitstep.mango.utils.mutator.exceptions;


import java.lang.reflect.Field;
import java.text.MessageFormat;

public class MutatorUnsupportedTypeException extends MutatorException {

	public MutatorUnsupportedTypeException(String canonicalName) {
		super(canonicalName);
	}

	public MutatorUnsupportedTypeException(Field field) {
		super(MessageFormat.format("{0}, unsupported type {1}", field.getName(), field.getType().getCanonicalName()));
	}
}
