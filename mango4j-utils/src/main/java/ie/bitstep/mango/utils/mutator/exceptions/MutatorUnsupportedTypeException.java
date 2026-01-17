package ie.bitstep.mango.utils.mutator.exceptions;


import java.lang.reflect.Field;
import java.text.MessageFormat;

public class MutatorUnsupportedTypeException extends MutatorException {

	/**
	 * Creates an exception for an unsupported type name.
	 *
	 * @param canonicalName the type name
	 */
	public MutatorUnsupportedTypeException(String canonicalName) {
		super(canonicalName);
	}

	/**
	 * Creates an exception for an unsupported field type.
	 *
	 * @param field the field with an unsupported type
	 */
	public MutatorUnsupportedTypeException(Field field) {
		super(MessageFormat.format("{0}, unsupported type {1}", field.getName(), field.getType().getCanonicalName()));
	}
}
