package ie.bitstep.mango.crypto.core.impl.service.encryption;

import ie.bitstep.mango.collections.ConcurrentCache;
import ie.bitstep.mango.crypto.core.CachedWrappedKeyHolder;
import ie.bitstep.mango.crypto.core.domain.CiphertextContainer;
import ie.bitstep.mango.crypto.core.domain.CryptoKey;
import ie.bitstep.mango.crypto.core.domain.HmacHolder;
import ie.bitstep.mango.crypto.core.encryption.EncryptionServiceDelegate;
import ie.bitstep.mango.crypto.core.enums.WrappedCryptoKeyTypes;
import ie.bitstep.mango.crypto.core.exceptions.NonTransientCryptoException;
import ie.bitstep.mango.crypto.core.formatters.CiphertextFormatter;
import ie.bitstep.mango.crypto.core.providers.CryptoKeyProvider;
import ie.bitstep.mango.crypto.core.utils.Generators;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.CIPHER_ALG;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.CIPHER_MODE;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.CIPHER_PADDING;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.CIPHER_TEXT;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.CONFIGURATION_ERROR;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.DATA_ENCRYPTION_KEY;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.DATA_ENCRYPTION_KEY_ID;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.GCM_TAG_LENGTH;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.IV;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.KEY_SIZE;
import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

@SuppressWarnings("squid:S1192") // duplicate from from non-cached implementation
public class CachedWrappedKeyEncryptionService extends EncryptionServiceDelegate {

	/**********************************************************************************
	 * NOTE:
	 * Although the cache field is static, it is initialized in the constructor
	 * to allow configuration through Spring. This is safe and acceptable in a Spring
	 * application since the class will typically be instantiated as a managed bean.
	 *********************************************************************************/
	private static final ConcurrentCache<String, CachedWrappedKeyHolder> CACHED_WRAPPED_KEY_HOLDER_CONCURRENT_CACHE = new ConcurrentCache<>(
			Duration.ofMillis(15),
			Duration.ofDays(1),
			Executors.newSingleThreadScheduledExecutor(),
			Clock.systemUTC());


	private final CryptoKeyProvider cryptoKeyProvider;
	private final CiphertextFormatter ciphertextFormatter;

	@SuppressWarnings("java:S3010")
	public CachedWrappedKeyEncryptionService(
			Duration entryTTL,
			Duration currentEntryTTL,
			CryptoKeyProvider cryptoKeyProvider,
			CiphertextFormatter ciphertextFormatter) {
		this.cryptoKeyProvider = cryptoKeyProvider;
		this.ciphertextFormatter = ciphertextFormatter;
		CACHED_WRAPPED_KEY_HOLDER_CONCURRENT_CACHE.setCacheEntryTTL(entryTTL);
		CACHED_WRAPPED_KEY_HOLDER_CONCURRENT_CACHE.setCurrentEntryTTL(currentEntryTTL);
	}

	public CachedWrappedKeyEncryptionService(CryptoKeyProvider cryptoKeyProvider, CiphertextFormatter ciphertextFormatter) {
		this(
				Duration.ofSeconds(15),
				Duration.ofDays(1),
				cryptoKeyProvider,
				ciphertextFormatter
		);
	}

	@Override
	public CiphertextContainer encrypt(final CryptoKey cryptoKey, final String payload) {
		try {
			final CryptoKeyConfiguration cep = createConfigPojo(cryptoKey, CryptoKeyConfiguration.class);
			final CipherConfig cipherConfig = CipherConfig.of(cep);
			final var iv = Generators.generateIV(cep.ivSize());
			final var wrappedKeyHolder = getCurrentWrappedKeyHolder(cep);
			final var dek = generateDataEncryptionKey(wrappedKeyHolder.key(), cipherConfig);
			final var cipher = CipherManager.getCipherInstance(cep.algorithm(), cep.mode(), cep.padding());

			CipherManager.initCipher(ENCRYPT_MODE, cipherConfig, iv, cipher, dek);

			final var encryptedBytes = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));

			return new CiphertextContainer(
					cryptoKey,
					Map.of(
							DATA_ENCRYPTION_KEY_ID, wrappedKeyHolder.keyId(),
							DATA_ENCRYPTION_KEY, ciphertextFormatter.format(wrappedKeyHolder.persistableEncryptedKey()),
							CIPHER_ALG, cep.algorithm().getAlgorithm(),
							CIPHER_MODE, cep.mode().getMode(),
							CIPHER_PADDING, cep.padding().getPadding(),
							KEY_SIZE, cep.keySize(),
							GCM_TAG_LENGTH, cep.gcmTagLength(),
							IV, Base64.getEncoder().encodeToString(iv),
							CIPHER_TEXT, Base64.getEncoder().encodeToString(encryptedBytes)));
		} catch (Exception e) {
			throw new NonTransientCryptoException(CONFIGURATION_ERROR, e);
		}
	}

	private CachedWrappedKeyHolder getCurrentWrappedKeyHolder(CryptoKeyConfiguration cep) {
		synchronized (CACHED_WRAPPED_KEY_HOLDER_CONCURRENT_CACHE) {
			CachedWrappedKeyHolder cachedWrappedKeyHolder = CACHED_WRAPPED_KEY_HOLDER_CONCURRENT_CACHE.getCurrent();
			if (cachedWrappedKeyHolder != null) {
				return cachedWrappedKeyHolder;
			} else {
				return newCachedWrappedKeyHolder(cep);
			}
		}
	}

	private CachedWrappedKeyHolder newCachedWrappedKeyHolder(CryptoKeyConfiguration cep) {
		// Current key not set or expired, create a new one and set as current
		final var keyId = UUID.randomUUID().toString();
		final var dek = Generators.generateRandomBits(cep.keySize());
		final var keyEncryptionKey = getWrappingKey(cep.keyEncryptionKey());

		return CACHED_WRAPPED_KEY_HOLDER_CONCURRENT_CACHE.putCurrent(keyId, new CachedWrappedKeyHolder(
						keyId,
						dek,
						super.encryptionService.encrypt(keyEncryptionKey, Base64.getEncoder().encodeToString(dek))
				)
		);
	}

	private CryptoKey getWrappingKey(String cryptoKeyId) {
		return cryptoKeyProvider.getById(cryptoKeyId);
	}

	@Override
	public String decrypt(final CiphertextContainer ciphertextContainer) {
		try {
			final EncryptedDataConfig edc = createConfigPojo(ciphertextContainer.getData(), EncryptedDataConfig.class);
			final CipherConfig cipherConfig = CipherConfig.of(edc);
			final var iv = Base64.getDecoder().decode(edc.iv());

			final CachedWrappedKeyHolder cachedWrappedKeyHolder = getWrappedKeyHolder(
					ciphertextContainer,
					edc);

			final var dek = new SecretKeySpec(cachedWrappedKeyHolder.key(), edc.algorithm().getAlgorithm());

			final var cipher = CipherManager.getCipherInstance(CipherConfig.of(edc));

			CipherManager.initCipher(DECRYPT_MODE, cipherConfig, iv, cipher, dek);

			final var decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(edc.cipherText()));

			return new String(decryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new NonTransientCryptoException(CONFIGURATION_ERROR, e);
		}
	}

	private CachedWrappedKeyHolder getWrappedKeyHolder(CiphertextContainer ciphertextContainer, EncryptedDataConfig edc) {
		synchronized (CACHED_WRAPPED_KEY_HOLDER_CONCURRENT_CACHE) {
			final String keyid = ciphertextContainer.getData().get(DATA_ENCRYPTION_KEY_ID).toString();
			CachedWrappedKeyHolder cachedWrappedKeyHolder = CACHED_WRAPPED_KEY_HOLDER_CONCURRENT_CACHE.get(keyid);

			if (cachedWrappedKeyHolder != null) {
				return cachedWrappedKeyHolder;
			} else {
				// key not cached, create holder and put it in cache
				final var encodedKey = super.encryptionService.decrypt(edc.dataEncryptionKey());
				final var decodedKey = Base64.getDecoder().decode(encodedKey.getBytes(StandardCharsets.UTF_8));

				cachedWrappedKeyHolder = new CachedWrappedKeyHolder(
						keyid,
						decodedKey,
						ciphertextContainer
				);

				return CACHED_WRAPPED_KEY_HOLDER_CONCURRENT_CACHE.put(cachedWrappedKeyHolder.keyId(), cachedWrappedKeyHolder);
			}
		}
	}

	@Override
	public void hmac(final Collection<HmacHolder> list) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String supportedCryptoKeyType() {
		return WrappedCryptoKeyTypes.CACHED_WRAPPED.getName();
	}

	static SecretKey generateDataEncryptionKey(final byte[] key, final CipherConfig cipherConfig) {
		return new SecretKeySpec(key, cipherConfig.algorithm().getAlgorithm());
	}
}
