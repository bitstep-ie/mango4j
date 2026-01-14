package ie.bitstep.mango.crypto.core.encryption;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import ie.bitstep.mango.crypto.core.domain.CiphertextContainer;
import ie.bitstep.mango.crypto.core.domain.CryptoKey;
import ie.bitstep.mango.crypto.core.domain.HmacHolder;
import ie.bitstep.mango.crypto.core.enums.Algorithm;
import ie.bitstep.mango.crypto.core.enums.Mode;
import ie.bitstep.mango.crypto.core.enums.NonProdCryptoKeyTypes;
import ie.bitstep.mango.crypto.core.enums.Padding;
import ie.bitstep.mango.crypto.core.exceptions.NonTransientCryptoException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;

import static ie.bitstep.mango.crypto.core.utils.Generators.generateIV;

public class PBKDF2EncryptionService extends EncryptionServiceDelegate {
	public static final String KEY_FACTORY = "PBKDF2WithHmacSHA256";
	public static final String PASS_PHRASE = "passPhrase";
	public static final String KEY_ALIAS = "keyAlias";
	public static final String GCM_TAG_LENGTH = "gcmTagLength";
	public static final String CIPHER_TEXT = "cipherText";
	public static final String IV = "iv";
	public static final String IV_SIZE = "ivSize";
	public static final String CIPHER_ALG = "algorithm";
	public static final String CIPHER_MODE = "mode";
	public static final String CIPHER_PADDING = "padding";
	public static final String KEY_SIZE = "keySize";
	public static final String ITERATIONS = "iterations";
	public static final String HASH_SALT = "salt";

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record CryptoKeyConfiguration(
			@JsonProperty(KEY_SIZE)
			int keySize,
			@JsonProperty(CIPHER_ALG)
			Algorithm algorithm,
			@JsonProperty(CIPHER_MODE)
			Mode mode,
			@JsonProperty(CIPHER_PADDING)
			Padding padding,
			@JsonProperty(ITERATIONS)
			int iterations,
			@JsonProperty(GCM_TAG_LENGTH)
			int gcmTagLength,
			@JsonProperty(PASS_PHRASE)
			String passPhrase,
			@JsonProperty(IV_SIZE)
			int ivSize,
			@JsonProperty(HASH_SALT)
			String salt) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record EncryptedDataConfig(
			@JsonProperty(KEY_SIZE)
			int keySize,
			@JsonProperty(CIPHER_ALG)
			Algorithm algorithm,
			@JsonProperty(CIPHER_MODE)
			Mode mode,
			@JsonProperty(CIPHER_PADDING)
			Padding padding,
			@JsonProperty(ITERATIONS)
			int iterations,
			@JsonProperty(GCM_TAG_LENGTH)
			int gcmTagLength,
			@JsonProperty(IV)
			String iv,
			@JsonProperty(CIPHER_TEXT)
			String cipherText) {
	}

	@Override
	@SuppressWarnings("java:S3329")
	public CiphertextContainer encrypt(final CryptoKey cryptoKey, final String payload) {
		try {
			final CryptoKeyConfiguration config = createConfigPojo(cryptoKey, CryptoKeyConfiguration.class);
			final var iv = generateIV(config.ivSize);
			final var secretKey = generatePBKDF2Key(
					config.keySize,
					config.iterations,
					config.algorithm.getAlgorithm(),
					config,
					iv);

			final var cipher = getCipherInstance(
					config.algorithm,
					config.mode,
					config.padding);

			if (config.mode == Mode.GCM) {
				final var gcmSpec = new GCMParameterSpec(config.gcmTagLength, iv);
				cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
			} else {
				final var ivSpec = new IvParameterSpec(iv);
				cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
			}

			final var encryptedBytes = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));

			return new CiphertextContainer(cryptoKey,
					Map.of(
							KEY_SIZE, config.keySize,
							CIPHER_ALG, config.algorithm.getAlgorithm(),
							CIPHER_MODE, config.mode.getMode(),
							CIPHER_PADDING, config.padding.getPadding(),
							ITERATIONS, config.iterations,
							GCM_TAG_LENGTH, config.gcmTagLength,
							IV, Base64.getEncoder().encodeToString(iv),
							CIPHER_TEXT, Base64.getEncoder().encodeToString(encryptedBytes)
					)
			);
		} catch (final Exception e) {
			throw new NonTransientCryptoException("Configuration error", e);
		}
	}

	@Override
	public String decrypt(final CiphertextContainer ciphertextContainer) {
		try {
			final EncryptedDataConfig config = createConfigPojo(ciphertextContainer.getData(), EncryptedDataConfig.class);
			final CryptoKeyConfiguration encryptionConfig = createConfigPojo(ciphertextContainer.getCryptoKey(), CryptoKeyConfiguration.class);
			final var iv = Base64.getDecoder().decode(config.iv);
			final var secretKey = generatePBKDF2Key(
					config.keySize,
					config.iterations,
					config.algorithm.getAlgorithm(),
					encryptionConfig,
					iv
			);

			final var cipher = getCipherInstance(ciphertextContainer.getData());

			if (config.mode == Mode.GCM) {
				final var gcmSpec = new GCMParameterSpec(config.gcmTagLength, iv);
				cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
			} else {
				final var ivSpec = new IvParameterSpec(iv); // NOSONAR:IV needs to be pulled from the cipher text
				cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
			}

			final var decodedBytes = Base64.getDecoder().decode(config.cipherText);
			final var decryptedBytes = cipher.doFinal(decodedBytes);

			return new String(decryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new NonTransientCryptoException("Configuration error", e);
		}
	}


	@Override
	public void hmac(final Collection<HmacHolder> list) {
		list.forEach(holder -> {
			final CryptoKeyConfiguration config = createConfigPojo(holder.getCryptoKey(), CryptoKeyConfiguration.class);
			try {
				final var secretKey = generatePBKDF2Key(
						config.keySize,
						config.iterations,
						config.algorithm.getAlgorithm(),
						config,
						config.salt.getBytes(StandardCharsets.UTF_8)
				);

				holder.setValue(computeHmacSha256(secretKey, holder.getValue()));
			} catch (Exception e) {
				throw new NonTransientCryptoException("Configuration error", e);
			}
		});
	}

	private static String computeHmacSha256(SecretKey secretKey, String payload) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), "HmacSHA256");
			mac.init(keySpec);
			return Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))); // or hex if preferred
		} catch (Exception e) {
			throw new IllegalStateException("HMAC computation failed", e);
		}
	}

	@Override
	public String supportedCryptoKeyType() {
		return NonProdCryptoKeyTypes.PBKDF2.getName();
	}

	private Cipher getCipherInstance(Algorithm algorithm, Mode mode, Padding padding) throws NoSuchPaddingException, NoSuchAlgorithmException {
		return Cipher.getInstance(
				algorithm.getAlgorithm() + "/" +
						mode.getMode() + "/" +
						padding.getPadding());
	}

	private Cipher getCipherInstance(Map<String, Object> data) throws NoSuchPaddingException, NoSuchAlgorithmException {
		return getCipherInstance(
				Algorithm.fromValue((String) data.get(CIPHER_ALG)),
				Mode.fromValue((String) data.get(CIPHER_MODE)),
				Padding.fromValue((String) data.get(CIPHER_PADDING)));
	}

	static SecretKey generatePBKDF2Key(
			final int keySize,
			final int iterations,
			final String algorithm,
			final CryptoKeyConfiguration config,
			final byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		final var spec = new PBEKeySpec(config.passPhrase.toCharArray(), salt, iterations, keySize);
		final var factory = SecretKeyFactory.getInstance(KEY_FACTORY);
		final var keyBytes = factory.generateSecret(spec).getEncoded();
		return new SecretKeySpec(keyBytes, algorithm);
	}
}