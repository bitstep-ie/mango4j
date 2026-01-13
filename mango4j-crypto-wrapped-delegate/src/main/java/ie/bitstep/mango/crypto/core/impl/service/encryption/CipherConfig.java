package ie.bitstep.mango.crypto.core.impl.service.encryption;

import ie.bitstep.mango.crypto.core.enums.Algorithm;
import ie.bitstep.mango.crypto.core.enums.Mode;
import ie.bitstep.mango.crypto.core.enums.Padding;

/**
 * Configuration holder for cipher operations.
 * <p>
 * Encapsulates the algorithm, mode, padding, and GCM tag length used for encryption/decryption.
 *
 * @param algorithm    the cryptographic algorithm to use (e.g., AES)
 * @param mode         the cipher mode of operation (e.g., CBC, GCM)
 * @param padding      the padding scheme to apply (e.g., PKCS5Padding)
 * @param gcmTagLength the length (in bits) of the authentication tag for GCM mode
 */
public record CipherConfig(
	Algorithm algorithm,
	Mode mode,
	Padding padding,
	int gcmTagLength) {

	static CipherConfig of(CryptoKeyConfiguration cep) {
		return new CipherConfig(
			cep.algorithm(),
			cep.mode(),
			cep.padding(),
			cep.gcmTagLength()
		);
	}

	static CipherConfig of(EncryptedDataConfig edc) {
		return new CipherConfig(
			edc.algorithm(),
			edc.mode(),
			edc.padding(),
			edc.gcmTagLength()
		);
	}
}