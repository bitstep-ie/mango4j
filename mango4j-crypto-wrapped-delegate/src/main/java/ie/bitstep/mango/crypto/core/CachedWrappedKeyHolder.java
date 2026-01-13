package ie.bitstep.mango.crypto.core;

import ie.bitstep.mango.crypto.core.domain.CiphertextContainer;

import java.util.UUID;

public class CachedWrappedKeyHolder implements AutoCloseable {
	private final String keyId;
	private final UUID vaultKeyId;
	private final CiphertextContainer persistableEncryptedKey;

	public CachedWrappedKeyHolder(
		String keyId,
		byte[] key,
		CiphertextContainer persistableEncryptedKey) {
		this.keyId = keyId;
		vaultKeyId = InMemoryKeyVault.INSTANCE.put(key);
		this.persistableEncryptedKey = persistableEncryptedKey;
	}

	@Override
	public void close() {
		InMemoryKeyVault.INSTANCE.remove(vaultKeyId);
	}

	public boolean equals(Object o) {
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}

		return this == o || this.keyId.equals(((CachedWrappedKeyHolder) o).keyId());
	}

	public int hashCode() {
		return keyId.hashCode();
	}

	public String toString() {
		return "WrappedKeyHolder(KeyId: " + keyId + ")";
	}

	public byte[] key() {
		return InMemoryKeyVault.INSTANCE.get(vaultKeyId);
	}

	public String keyId() {
		return keyId;
	}

	public CiphertextContainer persistableEncryptedKey() {
		return persistableEncryptedKey;
	}
}
