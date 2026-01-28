package ie.bitstep.mango.utils.masking;

import org.apache.commons.lang3.StringUtils;

/**
 * Masks the full value. (So only indicates whether the value is present.)
 */
public class FullMasker extends ParameterisedMasker {

	private static final String DEFAULT_FULL_MASKING_CHARACTERS = "***";

	/**
	 * Creates a full masker with default masking characters.
	 */
	public FullMasker() {
		super(DEFAULT_FULL_MASKING_CHARACTERS, 0, 0);
	}

	/**
	 * Creates a full masker with custom masking characters.
	 *
	 * @param maskingCharacters the masking characters
	 */
	public FullMasker(String maskingCharacters) {
		super(maskingCharacters, 0, 0);
	}

	/**
	 * Creates a full masker from a {@link Mask} annotation.
	 *
	 * @param mask the mask annotation
	 */
	public FullMasker(Mask mask) {
		this(mask.maskingChars());
	}

	/**
	 * Masks the value entirely.
	 *
	 * @param value the input value
	 * @return the masked value
	 */
	@Override
	public String mask(String value) {
		return StringUtils.isEmpty(value) ? value : maskingCharacters; // This looks like it disagrees with the class contract specified in the javadocs
	}
}
