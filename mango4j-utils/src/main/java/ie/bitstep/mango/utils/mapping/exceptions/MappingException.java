package ie.bitstep.mango.utils.mapping.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;

public class MappingException extends RuntimeException {
	/**
	 * Wraps a JSON processing exception.
	 *
	 * @param e the underlying exception
	 */
	public MappingException(JsonProcessingException e) {
		super(e);
	}
}
