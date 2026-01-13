package ie.bitstep.mango.utils.masking;

/**
 * A Masker for PANs.
 */
public class PanMasker extends ParameterisedMasker {

	private static final String DEFAULT_PAN_MASKING_CHARACTERS = "X";
	private static final int PAN_MASK_PREFIX_LENGTH = 6;
	private static final int PAN_MASK_SUFFIX_LENGTH = 4;

	public PanMasker() {
		this(DEFAULT_PAN_MASKING_CHARACTERS);
	}

	public PanMasker(String maskingCharacters) {
		super(maskingCharacters, PAN_MASK_PREFIX_LENGTH, PAN_MASK_SUFFIX_LENGTH);
	}

	public PanMasker(Mask mask) {
		this(mask.maskingChars());
	}


}
