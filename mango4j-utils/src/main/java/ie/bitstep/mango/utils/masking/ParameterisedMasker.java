package ie.bitstep.mango.utils.masking;

public class ParameterisedMasker implements Masker {

	protected final String maskingCharacters;
	protected final int nonMaskCharacterPrefixCount;
	protected final int nonMaskCharacterSuffixCount;

	public ParameterisedMasker(String maskingCharacters, int nonMaskCharacterPrefixCount, int nonMaskCharacterSuffixCount) {
		this.maskingCharacters = maskingCharacters;
		this.nonMaskCharacterPrefixCount = nonMaskCharacterPrefixCount;
		this.nonMaskCharacterSuffixCount = nonMaskCharacterSuffixCount;
	}

	public ParameterisedMasker(Mask mask) {
		this(mask.maskingChars(), mask.prefix(), mask.postfix());
	}

	@Override
	public String mask(String value) {
		return MaskingUtils.mask(value, maskingCharacters, nonMaskCharacterPrefixCount, nonMaskCharacterSuffixCount);
	}
}
