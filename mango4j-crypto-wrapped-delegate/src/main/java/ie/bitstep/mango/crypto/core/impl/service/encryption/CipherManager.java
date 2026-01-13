package ie.bitstep.mango.crypto.core.impl.service.encryption;

import ie.bitstep.mango.crypto.core.enums.Algorithm;
import ie.bitstep.mango.crypto.core.enums.Mode;
import ie.bitstep.mango.crypto.core.enums.Padding;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CipherManager {
	private CipherManager() {
		// NOSONAR
	}

	static void initCipher(int encryptMode, CipherConfig cep, byte[] iv, Cipher cipher, SecretKey dek) throws InvalidKeyException, InvalidAlgorithmParameterException {
		switch (cep.mode()) {
			case GCM -> {
				final var gcmSpec = new GCMParameterSpec(cep.gcmTagLength(), iv);
				cipher.init(encryptMode, dek, gcmSpec);
			}

			case CBC -> {
				final var ivSpec = new IvParameterSpec(iv);
				cipher.init(encryptMode, dek, ivSpec);
			}

			case NONE -> { // NOSONAR: DO NOTHING, special mode for testing failure scenarios
			}
		}
	}

	static Cipher getCipherInstance(Algorithm algorithm, Mode mode, Padding padding) throws NoSuchPaddingException, NoSuchAlgorithmException {
		return Cipher.getInstance(algorithm.getAlgorithm() + "/" + mode.getMode() + "/" + padding.getPadding());
	}

	static Cipher getCipherInstance(CipherConfig cc) throws NoSuchPaddingException, NoSuchAlgorithmException {
		return getCipherInstance(cc.algorithm(), cc.mode(), cc.padding());
	}
}
