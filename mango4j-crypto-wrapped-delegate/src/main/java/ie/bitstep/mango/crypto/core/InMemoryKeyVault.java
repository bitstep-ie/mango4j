package ie.bitstep.mango.crypto.core;

import ie.bitstep.mango.crypto.core.exceptions.NonTransientCryptoException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton InMemoryKeyVault.
 * - Encrypts raw keys with a random AES-256 vault key.
 * - Stores ciphertext + IV + vault key under a UUID.
 * - Provides dependency injection for testability.
 */
public enum InMemoryKeyVault {
	INSTANCE;

	private static final String ENCRYPTION_ALGORITHM = "AES";
	private static final int KEY_SIZE = 256;
	private static final int IV_LENGTH = 12;
	private static final String CIPHER_TRANSFORMATION_NAME = "AES/GCM/NoPadding";
	private static final int AUTHENTICATION_TAG_LENGTH = 128;

	// backing store
	private final Map<UUID, VaultEntry> store = new ConcurrentHashMap<>();
	private final KeyGenerator keyGenerator;
	private final SecureRandom random = new SecureRandom();

	InMemoryKeyVault() {
		try {
			this.keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
			keyGenerator.init(KEY_SIZE, random);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to init default KeyGenerator", e);
		}
	}

	/**
	 * Store raw key material; returns the UUID handle.
	 */
	@SuppressWarnings("java:S112")
	public UUID put(byte[] rawKeyBytes) {
		SecretKey vaultKey = keyGenerator.generateKey();
		byte[] iv = new byte[IV_LENGTH];
		random.nextBytes(iv);

		try {
			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_NAME);
			cipher.init(Cipher.ENCRYPT_MODE, vaultKey, new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH, iv));
			byte[] ciphertext = cipher.doFinal(rawKeyBytes);

			UUID id = UUID.randomUUID();
			store.put(id, new VaultEntry(vaultKey, iv, ciphertext));

			return id;
		} catch (Exception e) {
			throw new NonTransientCryptoException("An error occurred while encrypting the key for storage in the vault", e);
		}
	}

	/**
	 * Retrieve and decrypt the key, as raw bytes.
	 */
	@SuppressWarnings("java:S112")
	public byte[] get(UUID id) {
		VaultEntry entry = store.get(id);
		if (entry == null) return null; // NOSONAR: Needed to indicate non-existence

		try {
			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION_NAME);
			cipher.init(Cipher.DECRYPT_MODE, entry.vaultKey, new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH, entry.iv));
			return cipher.doFinal(entry.ciphertext);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void remove(UUID id) {
		store.remove(id);
	}

	public int size() {
		return store.size();
	}

	static final class VaultEntry {
		final SecretKey vaultKey;
		final byte[] iv;
		final byte[] ciphertext;

		VaultEntry(SecretKey vaultKey, byte[] iv, byte[] ciphertext) {
			this.vaultKey = vaultKey;
			this.iv = iv;
			this.ciphertext = ciphertext;
		}
	}
}