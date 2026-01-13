package ie.bitstep.mango.crypto.core.enums;

/**
 * Implementations that are supported natively by this library.
 */
public enum NonProdCryptoKeyTypes {
	BASE_64,
	PBKDF2,
	IDENTITY;

	public String getName() {
		return this.name();
	}
}