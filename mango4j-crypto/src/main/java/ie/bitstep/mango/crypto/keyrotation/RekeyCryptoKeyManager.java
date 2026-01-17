package ie.bitstep.mango.crypto.keyrotation;

import ie.bitstep.mango.crypto.core.domain.CryptoKey;

public interface RekeyCryptoKeyManager {
	/**
	 * Deletes a deprecated crypto key for a tenant.
	 *
	 * @param tenantsDeprecatedCryptoKey the deprecated key to delete
	 */
	void deleteKey(CryptoKey tenantsDeprecatedCryptoKey);
}
