package ie.bitstep.mango.crypto.core;

import ie.bitstep.mango.crypto.core.exceptions.NonTransientCryptoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.lang.reflect.Field;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class InMemoryKeyVaultTest {

	private static final UUID TEST_UUID = UUID.randomUUID();
	private static final String AES_GCM_NO_PADDING_TRANSFORMATION_VALUE = "AES/GCM/NoPadding";

	private Field keyGeneratorField;
	private Field secureRandomField;
	private Field storeField;

	@Mock
	private KeyGenerator mockKeyGeneratorMock;
	private KeyGenerator originalKeyGenerator;

	@Mock
	private SecureRandom mockSecureRandom;
	private SecureRandom originalSecureRandom;

	@Mock
	private SecretKey mockedSecretKey;

	@Mock
	private Cipher mockCipher;

	@Mock
	private Map<UUID, InMemoryKeyVault.VaultEntry> mockStore = new ConcurrentHashMap<>();
	private Map<UUID, InMemoryKeyVault.VaultEntry> originalStore;

	@Captor
	private ArgumentCaptor<GCMParameterSpec> cCMParameterSpecCaptor;

	@Captor
	private ArgumentCaptor<byte[]> byteCaptor;

	@Captor
	private ArgumentCaptor<UUID> uuidCaptor;

	@Captor
	private ArgumentCaptor<InMemoryKeyVault.VaultEntry> vaultEntryCaptor;

	private byte[] testKeyBytes;
	private byte[] encryptedKeyBytes;
	private byte[] testIv;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setup() throws NoSuchFieldException, IllegalAccessException {
		keyGeneratorField = InMemoryKeyVault.class.getDeclaredField("keyGenerator");
		keyGeneratorField.setAccessible(true);
		originalKeyGenerator = (KeyGenerator) keyGeneratorField.get(InMemoryKeyVault.INSTANCE);
		keyGeneratorField.set(InMemoryKeyVault.INSTANCE, mockKeyGeneratorMock);

		secureRandomField = InMemoryKeyVault.class.getDeclaredField("random");
		secureRandomField.setAccessible(true);
		originalSecureRandom = (SecureRandom) secureRandomField.get(InMemoryKeyVault.INSTANCE);
		secureRandomField.set(InMemoryKeyVault.INSTANCE, mockSecureRandom);

		storeField = InMemoryKeyVault.class.getDeclaredField("store");
		storeField.setAccessible(true);
		originalStore = (Map<UUID, InMemoryKeyVault.VaultEntry>) storeField.get(InMemoryKeyVault.INSTANCE);
		storeField.set(InMemoryKeyVault.INSTANCE, mockStore);

		testKeyBytes = encryptedKeyBytes = new byte[]{67, 33, 39};
		testIv = new byte[]{11, 25, 97};
		encryptedKeyBytes = new byte[]{1, 2, 3};
	}

	@AfterEach
	void resetEnum() throws IllegalAccessException {
		keyGeneratorField.set(InMemoryKeyVault.INSTANCE, originalKeyGenerator);
		secureRandomField.set(InMemoryKeyVault.INSTANCE, originalSecureRandom);
		storeField.set(InMemoryKeyVault.INSTANCE, originalStore);
	}

	@Test
	void initialState() {
		assertThat(originalStore).isInstanceOf(ConcurrentHashMap.class)
				.hasSize(0);
		assertThat(originalSecureRandom.getClass()).isEqualTo(SecureRandom.class);
	}

	@Test
	void put() throws Exception {
		try (MockedStatic<Cipher> cipherMocked = mockStatic(Cipher.class);
			 MockedStatic<UUID> uuidMocked = mockStatic(UUID.class)) {
			given(mockKeyGeneratorMock.generateKey()).willReturn(mockedSecretKey);
			cipherMocked.when(() -> Cipher.getInstance(AES_GCM_NO_PADDING_TRANSFORMATION_VALUE)).thenReturn(mockCipher);
			uuidMocked.when(UUID::randomUUID).thenReturn(TEST_UUID);
			given(mockCipher.doFinal(testKeyBytes)).willReturn(encryptedKeyBytes);

			assertThat(InMemoryKeyVault.INSTANCE.put(testKeyBytes)).isEqualTo(TEST_UUID);

			then(mockSecureRandom).should().nextBytes(byteCaptor.capture());
			assertThat(byteCaptor.getValue()).isEqualTo(new byte[12]);

			then(mockCipher).should().init(eq(Cipher.ENCRYPT_MODE), eq(mockedSecretKey), cCMParameterSpecCaptor.capture());
			assertThat(cCMParameterSpecCaptor.getValue().getTLen()).isEqualTo(128);
			assertThat(cCMParameterSpecCaptor.getValue().getIV()).isEqualTo(byteCaptor.getValue());

			then(mockCipher).should().doFinal(testKeyBytes);

			then(mockStore).should().put(uuidCaptor.capture(), vaultEntryCaptor.capture());
			assertThat(uuidCaptor.getValue()).isEqualTo(TEST_UUID);
			assertThat(vaultEntryCaptor.getValue().vaultKey).isEqualTo(mockedSecretKey);
			assertThat(vaultEntryCaptor.getValue().iv).isEqualTo(byteCaptor.getValue());
			assertThat(vaultEntryCaptor.getValue().ciphertext).isEqualTo(encryptedKeyBytes);
		}
	}

	@Test
	void putException() {
		RuntimeException testCause = new RuntimeException();
		try (MockedStatic<Cipher> mockedCipher = mockStatic(Cipher.class)) {
			given(mockKeyGeneratorMock.generateKey()).willReturn(mockedSecretKey);
			mockedCipher.when(() -> Cipher.getInstance(AES_GCM_NO_PADDING_TRANSFORMATION_VALUE)).thenThrow(testCause);

			assertThatThrownBy(() -> InMemoryKeyVault.INSTANCE.put(testKeyBytes))
					.isInstanceOf(NonTransientCryptoException.class)
					.hasMessage("An error occurred while encrypting the key for storage in the vault")
					.hasCause(testCause);

			then(mockSecureRandom).should().nextBytes(byteCaptor.capture());
			assertThat(byteCaptor.getValue().length).isEqualTo(12);
			then(mockCipher).shouldHaveNoInteractions();
			then(mockStore).shouldHaveNoInteractions();
		}
	}

	@Test
	void testGetCipher() throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
		try (MockedStatic<Cipher> mockedCipher = mockStatic(Cipher.class)) {
			InMemoryKeyVault.VaultEntry existingVaultEntry = new InMemoryKeyVault.VaultEntry(mockedSecretKey, testIv, encryptedKeyBytes);
			given(mockStore.get(TEST_UUID)).willReturn(existingVaultEntry);
			mockedCipher.when(() -> Cipher.getInstance(AES_GCM_NO_PADDING_TRANSFORMATION_VALUE)).thenReturn(mockCipher);
			given(mockCipher.doFinal(encryptedKeyBytes)).willReturn(testKeyBytes);

			assertThat(InMemoryKeyVault.INSTANCE.get(TEST_UUID)).isEqualTo(testKeyBytes);

			then(mockCipher).should().init(eq(Cipher.DECRYPT_MODE), eq(mockedSecretKey), cCMParameterSpecCaptor.capture());
			assertThat(cCMParameterSpecCaptor.getValue().getTLen()).isEqualTo(128);
			assertThat(cCMParameterSpecCaptor.getValue().getIV()).isEqualTo(testIv);
			then(mockCipher).should().doFinal(encryptedKeyBytes);
		}
	}

	@Test
	void testGetCipherFails() {
		try (MockedStatic<Cipher> mockedCipher = mockStatic(Cipher.class)) {
			IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Failed!!");
			given(mockStore.get(TEST_UUID)).willReturn(new InMemoryKeyVault.VaultEntry(null, new byte[]{}, new byte[]{}));
			mockedCipher.when(() -> Cipher.getInstance(AES_GCM_NO_PADDING_TRANSFORMATION_VALUE)).thenThrow(illegalArgumentException);

			assertThatThrownBy(() -> InMemoryKeyVault.INSTANCE.get(TEST_UUID))
					.isInstanceOf(RuntimeException.class)
					.hasCause(illegalArgumentException);
		}
	}

	@Test
	void remove() {
		InMemoryKeyVault.VaultEntry testVaultEntry = new InMemoryKeyVault.VaultEntry(null, new byte[]{}, new byte[]{});
		given(mockStore.remove(TEST_UUID)).willReturn(testVaultEntry);

		InMemoryKeyVault.INSTANCE.remove(TEST_UUID);

		then(mockStore).should().remove(TEST_UUID);
	}

	@Test
	void size() {
		given(mockStore.size()).willReturn(7);

		assertThat(InMemoryKeyVault.INSTANCE.size()).isEqualTo(7);
		assertNull(InMemoryKeyVault.INSTANCE.get(TEST_UUID));
	}
}
