package ie.bitstep.mango.crypto;

import ie.bitstep.mango.crypto.core.domain.CryptoKey;
import ie.bitstep.mango.crypto.hmac.HmacStrategy;
import ie.bitstep.mango.crypto.hmac.ListHmacFieldStrategy;
import ie.bitstep.mango.crypto.hmac.RekeyListHmacFieldStrategy;

import java.util.List;
import java.util.Optional;

public class RekeyCryptoShield {
	private final CryptoShield cryptoShield;
	private final CryptoShieldDelegate rekeyCryptoShieldDelegate;

	/**
	 * Creates a rekeying wrapper over an existing {@link CryptoShield}.
	 *
	 * @param cryptoShield          the base crypto shield to delegate to
	 * @param currentEncryptionKey  the encryption key to use during rekeying
	 * @param currentHmacKeys       the HMAC keys to use during rekeying
	 */
	public RekeyCryptoShield(CryptoShield cryptoShield,
							 CryptoKey currentEncryptionKey,
							 List<CryptoKey> currentHmacKeys) {
		this.cryptoShield = cryptoShield;
		this.rekeyCryptoShieldDelegate = new CryptoShieldDelegate() {

			/**
			 * Returns the rekeying encryption key.
			 *
			 * @return the encryption key to use
			 */
			@Override
			public CryptoKey getCurrentEncryptionKey() {
				return currentEncryptionKey;
			}

			/**
			 * Returns the rekeying HMAC strategy for the given entity, wrapping list strategies if required.
			 *
			 * @param entity the entity to inspect
			 * @return the HMAC strategy to use
			 */
			@Override
			public Optional<HmacStrategy> getHmacStrategy(Object entity) {
				Optional<HmacStrategy> hmacStrategy = cryptoShield.getHmacStrategy(entity);
				if (hmacStrategy.isPresent() &&
					!currentHmacKeys.isEmpty() &&
					hmacStrategy.get().getClass().isAssignableFrom(ListHmacFieldStrategy.class)) {
					RekeyListHmacFieldStrategy rekeyListHmacFieldStrategy = new RekeyListHmacFieldStrategy((ListHmacFieldStrategy) hmacStrategy.get(), currentHmacKeys);
					hmacStrategy = Optional.of(rekeyListHmacFieldStrategy);
				}
				return hmacStrategy;
			}
		};
	}

	/**
	 * Decrypts the provided entity using the underlying {@link CryptoShield}.
	 *
	 * @param entity the entity to decrypt
	 */
	public void decrypt(Object entity) {
		cryptoShield.decrypt(entity);
	}

	/**
	 * Encrypts and HMACs the entity using rekey-specific keys.
	 *
	 * @param entity the entity to protect
	 */
	public void protect(Object entity) {
		cryptoShield.encrypt(entity, rekeyCryptoShieldDelegate);
	}
}
