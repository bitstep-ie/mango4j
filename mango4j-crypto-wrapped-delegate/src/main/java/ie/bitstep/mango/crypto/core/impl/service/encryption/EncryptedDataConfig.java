package ie.bitstep.mango.crypto.core.impl.service.encryption;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import ie.bitstep.mango.crypto.core.enums.Algorithm;
import ie.bitstep.mango.crypto.core.enums.Mode;
import ie.bitstep.mango.crypto.core.enums.Padding;

import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.CIPHER_ALG;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.CIPHER_MODE;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.CIPHER_PADDING;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.CIPHER_TEXT;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.DATA_ENCRYPTION_KEY;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.GCM_TAG_LENGTH;
import static ie.bitstep.mango.crypto.core.impl.service.encryption.WrappedEncryptionConstants.IV;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EncryptedDataConfig(
	@JsonProperty(DATA_ENCRYPTION_KEY)
	String dataEncryptionKey,
	@JsonProperty(CIPHER_ALG)
	Algorithm algorithm,
	@JsonProperty(CIPHER_MODE)
	Mode mode,
	@JsonProperty(CIPHER_PADDING)
	Padding padding,
	@JsonProperty(GCM_TAG_LENGTH)
	int gcmTagLength,
	@JsonProperty(IV)
	String iv,
	@JsonProperty(CIPHER_TEXT)
	String cipherText
) {
}
