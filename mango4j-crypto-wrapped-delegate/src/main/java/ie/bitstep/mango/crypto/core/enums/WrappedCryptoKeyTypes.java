package ie.bitstep.mango.crypto.core.enums;

/**
 * Implementations that are supported natively by this library.
 */
public enum WrappedCryptoKeyTypes { // NOSONAR: Single crypto key type supported
	WRAPPED,
	CACHED_WRAPPED;

	public String getName() {
		return this.name();
	}
}