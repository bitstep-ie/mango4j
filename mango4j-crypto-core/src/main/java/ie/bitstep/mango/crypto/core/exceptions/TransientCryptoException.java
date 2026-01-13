package ie.bitstep.mango.crypto.core.exceptions;

import ie.bitstep.mango.crypto.core.encryption.EncryptionServiceDelegate;

/**
 * Exception that might be thrown by cryptographic operation methods in
 * {@link EncryptionServiceDelegate EncryptionServiceDelegates}
 * for some operation that may be retried.
 */
public class TransientCryptoException extends RuntimeException {

	public TransientCryptoException(String message, Throwable cause) {
		super(message, cause);
	}
}