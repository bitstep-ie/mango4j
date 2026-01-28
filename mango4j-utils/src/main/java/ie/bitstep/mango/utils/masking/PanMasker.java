package ie.bitstep.mango.utils.masking;

/**
 * A Masker for PANs.
 */
public class PanMasker extends ParameterisedMasker {

	private static final String DEFAULT_PAN_MASKING_CHARACTERS = "X";
	private static final int PAN_MASK_PREFIX_LENGTH = 6;
	private static final int PAN_MASK_SUFFIX_LENGTH = 4;

	/**
	 * Creates a PAN masker with default settings.
	 */
	public PanMasker() {
		this(DEFAULT_PAN_MASKING_CHARACTERS);
	}

	/**
	 * Creates a PAN masker with custom masking characters.
	 *
	 * @param maskingCharacters the masking characters
	 */
	public PanMasker(String maskingCharacters) {
		super(maskingCharacters, PAN_MASK_PREFIX_LENGTH, PAN_MASK_SUFFIX_LENGTH);
	}

	/**
	 * Creates a PAN masker from a {@link Mask} annotation.
	 *
	 * @param mask the mask annotation
	 */
	public PanMasker(Mask mask) {
		this(mask.maskingChars());
	}


}
