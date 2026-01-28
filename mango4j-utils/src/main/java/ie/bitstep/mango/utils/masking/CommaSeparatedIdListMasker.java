package ie.bitstep.mango.utils.masking;

/**
 * Created by e048222 on 5/19/2017.
 */
public class CommaSeparatedIdListMasker extends AbstractCommaSeparatedListMasker {

	/**
	 * Creates a masker for comma-separated IDs with default settings.
	 */
	public CommaSeparatedIdListMasker() {
		super(new IdMasker());
	}

	/**
	 * Creates a masker for comma-separated IDs with custom masking characters.
	 *
	 * @param maskingCharacters the masking characters
	 */
	public CommaSeparatedIdListMasker(String maskingCharacters) {
		super(new IdMasker(maskingCharacters));
	}

	/**
	 * Creates a masker from a {@link Mask} annotation.
	 *
	 * @param mask the mask annotation
	 */
	public CommaSeparatedIdListMasker(Mask mask) {
		this(mask.maskingChars());
	}
}
