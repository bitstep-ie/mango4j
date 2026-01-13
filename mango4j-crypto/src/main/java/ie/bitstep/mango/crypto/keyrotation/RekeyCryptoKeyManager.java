package ie.bitstep.mango.crypto.keyrotation;

import ie.bitstep.mango.crypto.core.domain.CryptoKey;

public interface RekeyCryptoKeyManager {
	void deleteKey(CryptoKey tenantsDeprecatedCryptoKey);
}
