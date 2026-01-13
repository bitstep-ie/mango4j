package ie.bitstep.mango.crypto;

import ie.bitstep.mango.crypto.core.encryption.EncryptionService;
import ie.bitstep.mango.crypto.core.providers.CryptoKeyProvider;

public record HmacStrategyHelper(EncryptionService encryptionService, CryptoKeyProvider cryptoKeyProvider) {
}