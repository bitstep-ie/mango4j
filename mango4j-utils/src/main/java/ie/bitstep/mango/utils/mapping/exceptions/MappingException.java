package ie.bitstep.mango.utils.mapping.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;

public class MappingException extends RuntimeException {
	public MappingException(JsonProcessingException e) {
		super(e);
	}
}
