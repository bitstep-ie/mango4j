package ie.bitstep.mango.crypto.core.exceptions;

/**
 * Exception thrown if there was some problem trying to parse or format a ciphertext String
 */
public class CiphertextFormatterException extends NonTransientCryptoException {

	public CiphertextFormatterException(String message) {
		super(message);
	}

	public CiphertextFormatterException(String message, Exception cause) {
		super(message, cause);
	}
}
