package ie.bitstep.mango.crypto.core.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Mode {
	/**
	 * No mode, used for testing negative scenarios
	 */
	NONE("NONE"),
	/**
	 * CBC mode - <a href="https://www.sciencedirect.com/topics/mathematics/cipher-block-chaining">Cipher block chaining </a>
	 */
	CBC("CBC"),
	/**
	 * GCM - <a href="https://xilinx.github.io/Vitis_Libraries/security/2019.2/guide_L1/internals/gcm.html">Galois/Counter Mode</a>
	 */
	GCM("GCM");

	private final String mode; // NOSONAR

	Mode(String mode) {
		this.mode = mode;
	}

	@JsonValue
	public String getMode() {
		return mode;
	}

	@JsonCreator
	public static Mode fromValue(String value) {
		for (Mode v : values()) {
			if (v.getMode().equals(value)) {
				return v;
			}
		}

		throw new IllegalArgumentException("No enum constant with value: " + value);
	}
}

