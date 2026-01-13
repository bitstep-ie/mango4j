package ie.bitstep.mango.crypto.exceptions;

import ie.bitstep.mango.crypto.core.exceptions.NonTransientCryptoException;

public class DeserializationException extends NonTransientCryptoException {
	public DeserializationException(String message) {
		super(message);
	}
}