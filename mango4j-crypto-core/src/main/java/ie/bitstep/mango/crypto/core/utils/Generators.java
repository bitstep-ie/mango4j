package ie.bitstep.mango.crypto.core.utils;

import java.security.SecureRandom;

public class Generators {
	private Generators() {
		// NOSONAR
	}

	public static byte[] generateRandomBytes(int size) {
		final var secureRandomKeyBytes = new byte[size];
		final var secureRandom = new SecureRandom();
		secureRandom.nextBytes(secureRandomKeyBytes);

		return secureRandomKeyBytes;
	}

	public static byte[] generateRandomBits(int size) {
		final var secureRandomKeyBytes = new byte[size / 8];
		final var secureRandom = new SecureRandom();
		secureRandom.nextBytes(secureRandomKeyBytes);

		return secureRandomKeyBytes;
	}

	public static byte[] generateIV(final int length) {
		return generateRandomBytes(length);
	}
}
