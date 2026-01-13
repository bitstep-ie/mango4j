package ie.bitstep.mango.crypto.core.exceptions;

import ie.bitstep.mango.crypto.core.providers.CryptoKeyProvider;

/**
 * Exception thrown when this library unsuccessfully tries to get an active encryption key from the application's
 * {@link CryptoKeyProvider CryptoKeyProvider} implementation
 */
public class ActiveEncryptionKeyNotFoundException extends NonTransientCryptoException {
	public ActiveEncryptionKeyNotFoundException() {
		super("No active encryption key was found");
	}
}
