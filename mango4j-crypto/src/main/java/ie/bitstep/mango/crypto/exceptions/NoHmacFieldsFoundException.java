package ie.bitstep.mango.crypto.exceptions;

import ie.bitstep.mango.crypto.core.exceptions.NonTransientCryptoException;

public class NoHmacFieldsFoundException extends NonTransientCryptoException {
	public NoHmacFieldsFoundException(String message) {
		super(message);
	}
}
