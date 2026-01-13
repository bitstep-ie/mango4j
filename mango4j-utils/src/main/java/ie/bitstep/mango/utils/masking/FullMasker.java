package ie.bitstep.mango.utils.masking;

import org.apache.commons.lang3.StringUtils;

/**
 * Masks the full value. (So only indicates whether the value is present.)
 */
public class FullMasker extends ParameterisedMasker {

	private static final String DEFAULT_FULL_MASKING_CHARACTERS = "***";

	public FullMasker() {
		super(DEFAULT_FULL_MASKING_CHARACTERS, 0, 0);
	}

	public FullMasker(String maskingCharacters) {
		super(maskingCharacters, 0, 0);
	}

	public FullMasker(Mask mask) {
		this(mask.maskingChars());
	}

	@Override
	public String mask(String value) {
		return StringUtils.isEmpty(value) ? value : maskingCharacters; // This looks like it disagrees with the class contract specified in the javadocs
	}
}
