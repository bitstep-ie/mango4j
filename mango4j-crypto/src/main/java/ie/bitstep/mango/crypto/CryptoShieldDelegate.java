package ie.bitstep.mango.crypto;

import ie.bitstep.mango.crypto.core.domain.CryptoKey;
import ie.bitstep.mango.crypto.hmac.HmacStrategy;

import java.util.Optional;

public interface CryptoShieldDelegate {
	CryptoKey getCurrentEncryptionKey();

	Optional<HmacStrategy> getHmacStrategy(Object entity);
}
