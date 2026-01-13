package ie.bitstep.mango.crypto.core.exceptions;

import ie.bitstep.mango.crypto.core.encryption.EncryptionServiceDelegate;

/**
 * Exception that might be thrown by cryptographic operation methods in
 * {@link EncryptionServiceDelegate EncryptionServiceDelegates}
 * for some operation that <u>should <b><i>not</i></b> be retried</u>.
 */
public class NonTransientCryptoException extends RuntimeException {

	public NonTransientCryptoException(String message, Throwable cause) {
		super(message, cause);
	}

	public NonTransientCryptoException(String message) {
		super(message);
	}
}