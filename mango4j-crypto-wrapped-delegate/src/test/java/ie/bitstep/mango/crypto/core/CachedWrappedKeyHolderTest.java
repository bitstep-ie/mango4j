package ie.bitstep.mango.crypto.core;

import ie.bitstep.mango.crypto.core.domain.CiphertextContainer;
import ie.bitstep.mango.crypto.core.domain.CryptoKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class CachedWrappedKeyHolderTest {

	public static final UUID TEST_KEY_ID = UUID.randomUUID();

	@SuppressWarnings("unchecked")
	@AfterEach
	void tearDown() throws NoSuchFieldException, IllegalAccessException {
		InMemoryKeyVault.INSTANCE.remove(TEST_KEY_ID);
		Field keyGeneratorField = InMemoryKeyVault.class.getDeclaredField("store");
		keyGeneratorField.setAccessible(true);
		((Map<UUID, InMemoryKeyVault.VaultEntry>) keyGeneratorField.get(InMemoryKeyVault.INSTANCE)).clear();
	}

	private static CiphertextContainer createCryptoKeyContainer() {
		return new CiphertextContainer(
				new CryptoKey(),
				new LinkedHashMap<>()
		);
	}

	@Test
	void testAutoClose() {
		String keyId = TEST_KEY_ID.toString();
		int sizeBefore = InMemoryKeyVault.INSTANCE.size();
		CachedWrappedKeyHolder cachedWrappedKeyHolder = spy(
				new CachedWrappedKeyHolder(
						keyId,
						new byte[32],
						createCryptoKeyContainer()));

		try (cachedWrappedKeyHolder) {
			// NOSONAR: intentionally empty
		}

		assertThat(InMemoryKeyVault.INSTANCE.size()).isEqualTo(sizeBefore);
		verify(cachedWrappedKeyHolder).close();
	}

	@Test
	void testEquals() {
		String keyId = TEST_KEY_ID.toString();

		CachedWrappedKeyHolder cachedWrappedKeyHolder1 = new CachedWrappedKeyHolder(
				keyId,
				new byte[32],
				createCryptoKeyContainer());

		CachedWrappedKeyHolder cachedWrappedKeyHolder2 = new CachedWrappedKeyHolder(
				keyId,
				new byte[32],
				createCryptoKeyContainer());

		assertThat(cachedWrappedKeyHolder1).isEqualTo(cachedWrappedKeyHolder2);
	}

	@Test
	void testNotEquals1() {
		CachedWrappedKeyHolder cachedWrappedKeyHolder1 = new CachedWrappedKeyHolder(
				UUID.randomUUID().toString(),
				new byte[32],
				createCryptoKeyContainer());

		CachedWrappedKeyHolder cachedWrappedKeyHolder2 = new CachedWrappedKeyHolder(
				TEST_KEY_ID.toString(),
				new byte[32],
				createCryptoKeyContainer());

		assertThat(cachedWrappedKeyHolder1).isNotEqualTo(cachedWrappedKeyHolder2);
	}

	@Test
	void testNotEquals2() {
		CachedWrappedKeyHolder cachedWrappedKeyHolder1 = new CachedWrappedKeyHolder(
				TEST_KEY_ID.toString(),
				new byte[32],
				createCryptoKeyContainer());

		assertThat(cachedWrappedKeyHolder1).isNotEqualTo(null);
	}

	@Test
	void testNotEquals3() {
		CachedWrappedKeyHolder cachedWrappedKeyHolder1 = new CachedWrappedKeyHolder(
				TEST_KEY_ID.toString(),
				new byte[32],
				createCryptoKeyContainer());

		assertThat(cachedWrappedKeyHolder1).isNotEqualTo(new String());
	}

	@Test
	void testIdentity() {
		CachedWrappedKeyHolder cachedWrappedKeyHolder = new CachedWrappedKeyHolder(
				TEST_KEY_ID.toString(),
				new byte[32],
				createCryptoKeyContainer());

		assertThat(cachedWrappedKeyHolder).isEqualTo(cachedWrappedKeyHolder); // NOSONAR
	}

	@Test
	void testHashCode() {
		CachedWrappedKeyHolder cachedWrappedKeyHolder = new CachedWrappedKeyHolder(
				"0aa09e28-49b0-491a-9211-be0742174f28",
				new byte[32],
				createCryptoKeyContainer());

		assertThat(cachedWrappedKeyHolder.hashCode()).isEqualTo(-72194015);
	}

	@Test
	void testToString() {
		CachedWrappedKeyHolder cachedWrappedKeyHolder = new CachedWrappedKeyHolder(
				"0aa09e28-49b0-491a-9211-be0742174f28",
				new byte[32],
				createCryptoKeyContainer());

		assertThat(cachedWrappedKeyHolder).hasToString("WrappedKeyHolder(KeyId: 0aa09e28-49b0-491a-9211-be0742174f28)");
	}
}