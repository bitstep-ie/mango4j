package ie.bitstep.mango.crypto.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Algorithms supported by the mango-crypto-core PBKDFS2 &amp; WrapperKey encryption services
 */
public enum Algorithm {
	/**
	 * No algorithm, used for testing negative scenarios
	 */
	NONE("NONE"),
	/**
	 * Standard AES, Advanced Encryption Standard
	 */
	AES("AES"),
	/**
	 * Standard DES, Data Encryption Standard (don't use this)
	 */
	DES("DES"),
	/**
	 * Officially known as TripleDES (DES-Encrypt-Decrypt-Encrypt), Data Encryption Standard (don't use this)
	 */
	DES_EDE("DESede");

	private final String algorithm; // NOSONAR

	Algorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	@JsonValue
	public String getAlgorithm() {
		return algorithm;
	}

	@JsonCreator
	public static Algorithm fromValue(String value) {
		for (Algorithm v : values()) {
			if (v.getAlgorithm().equals(value)) {
				return v;
			}
		}

		throw new IllegalArgumentException("No enum constant with value: " + value);
	}
}
