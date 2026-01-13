package ie.bitstep.mango.crypto.exceptions;

import ie.bitstep.mango.crypto.core.exceptions.NonTransientCryptoException;

public class InvalidUniqueGroupDefinition extends NonTransientCryptoException {
	public InvalidUniqueGroupDefinition(String message) {
		super(message);
	}
}