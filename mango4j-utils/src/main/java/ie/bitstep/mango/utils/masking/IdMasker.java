package ie.bitstep.mango.utils.masking;

/**
 * A Masker for IDs.
 */
public class IdMasker extends ParameterisedMasker {

	private static final String DEFAULT_ID_MASKING_CHARACTERS = "X";
	private static final int ID_NON_MASK_CHARACTER_PREFIX_COUNT = 5;
	private static final int ID_NON_MASK_CHARACTER_SUFFIX_COUNT = 0;

	/**
	 * Creates an ID masker with default settings.
	 */
	public IdMasker() {
		this(DEFAULT_ID_MASKING_CHARACTERS);
	}

	/**
	 * Creates an ID masker from a {@link Mask} annotation.
	 *
	 * @param mask the mask annotation
	 */
	public IdMasker(Mask mask) {
		this(mask.maskingChars());
	}

	/**
	 * Creates an ID masker with custom masking characters.
	 *
	 * @param maskingCharacters the masking characters
	 */
	public IdMasker(String maskingCharacters) {
		super(maskingCharacters, ID_NON_MASK_CHARACTER_PREFIX_COUNT, ID_NON_MASK_CHARACTER_SUFFIX_COUNT);
	}
}
