package ie.bitstep.mango.crypto.core.impl.service.encryption;

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
import java.util.Base64;
import java.util.Collection;
import java.util.Map;

import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.CIPHER_ALG;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.CIPHER_MODE;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.CIPHER_PADDING;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.CIPHER_TEXT;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.CONFIGURATION_ERROR;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.DATA_ENCRYPTION_KEY;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.GCM_TAG_LENGTH;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.IV;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.KEY_SIZE;
import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

public class WrappedKeyEncryptionService extends EncryptionServiceDelegate {

	private final CryptoKeyProvider cryptoKeyProvider;
	private final CiphertextFormatter ciphertextFormatter;

	public WrappedKeyEncryptionService(CryptoKeyProvider cryptoKeyProvider, CiphertextFormatter ciphertextFormatter) {
		this.cryptoKeyProvider = cryptoKeyProvider;
		this.ciphertextFormatter = ciphertextFormatter;
	}

	@Override
	public CiphertextContainer encrypt(final CryptoKey cryptoKey, final String payload) {
		try {
			final CryptoKeyConfiguration cep = createConfigPojo(cryptoKey, CryptoKeyConfiguration.class);
			final CipherConfig cipherConfig = CipherConfig.of(cep);
			final var iv = Generators.generateIV(cep.ivSize());
			final var dek = generateDataEncryptionKey(cep.keySize(), cipherConfig);

			final var cipher = CipherManager.getCipherInstance(cep.algorithm(), cep.mode(), cep.padding());

			CipherManager.initCipher(ENCRYPT_MODE, cipherConfig, iv, cipher, dek);

			final var encryptedBytes = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));

			final var keyEncryptionKey = getWrappingKey(cep.keyEncryptionKey());
			final var encryptedDek = super.encryptionService.encrypt(keyEncryptionKey, Base64.getEncoder().encodeToString(dek.getEncoded()));

			return new CiphertextContainer(
				cryptoKey,
				Map.of(DATA_ENCRYPTION_KEY, ciphertextFormatter.format(encryptedDek),
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

	private CryptoKey getWrappingKey(String cryptoKeyId) {
		return cryptoKeyProvider.getById(cryptoKeyId);
	}

	@Override
	public String decrypt(final CiphertextContainer ciphertextContainer) {
		try {
			final EncryptedDataConfig edc = createConfigPojo(ciphertextContainer.getData(), EncryptedDataConfig.class);
			final CipherConfig cipherConfig = CipherConfig.of(edc);
			final var iv = Base64.getDecoder().decode(edc.iv());
			final var dekKeyEncoded = super.encryptionService.decrypt(edc.dataEncryptionKey());
			final var dekKeyBytes = Base64.getDecoder().decode(dekKeyEncoded.getBytes(StandardCharsets.UTF_8));
			final var dek = new SecretKeySpec(dekKeyBytes, edc.algorithm().getAlgorithm());

			final var cipher = CipherManager.getCipherInstance(CipherConfig.of(edc));

			CipherManager.initCipher(DECRYPT_MODE, cipherConfig, iv, cipher, dek);

			final var decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(edc.cipherText()));

			return new String(decryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new NonTransientCryptoException(CONFIGURATION_ERROR, e);
		}
	}

	@Override
	public void hmac(final Collection<HmacHolder> list) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String supportedCryptoKeyType() {
		return WrappedCryptoKeyTypes.WRAPPED.getName();
	}


	static SecretKey generateDataEncryptionKey(final int keySize, final CipherConfig cipherConfig) {
		return new SecretKeySpec(Generators.generateRandomBits(keySize), cipherConfig.algorithm().getAlgorithm());
	}
}
