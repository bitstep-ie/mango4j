package ie.bitstep.mango.utils.masking;

public class ParameterisedMasker implements Masker {

	protected final String maskingCharacters;
	protected final int nonMaskCharacterPrefixCount;
	protected final int nonMaskCharacterSuffixCount;

	/**
	 * Creates a masker with explicit masking configuration.
	 *
	 * @param maskingCharacters the masking characters
	 * @param nonMaskCharacterPrefixCount prefix length to keep
	 * @param nonMaskCharacterSuffixCount suffix length to keep
	 */
	public ParameterisedMasker(String maskingCharacters, int nonMaskCharacterPrefixCount, int nonMaskCharacterSuffixCount) {
		this.maskingCharacters = maskingCharacters;
		this.nonMaskCharacterPrefixCount = nonMaskCharacterPrefixCount;
		this.nonMaskCharacterSuffixCount = nonMaskCharacterSuffixCount;
	}

	/**
	 * Creates a masker from a {@link Mask} annotation.
	 *
	 * @param mask the mask annotation
	 */
	public ParameterisedMasker(Mask mask) {
		this(mask.maskingChars(), mask.prefix(), mask.postfix());
	}

	/**
	 * Masks the supplied value.
	 *
	 * @param value the input value
	 * @return the masked value
	 */
	@Override
	public String mask(String value) {
		return MaskingUtils.mask(value, maskingCharacters, nonMaskCharacterPrefixCount, nonMaskCharacterSuffixCount);
	}
}
