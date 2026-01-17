package ie.bitstep.mango.utils.masking;

/**
 * Created by e048222 on 5/19/2017.
 */
public class CommaSeparatedPanListMasker extends AbstractCommaSeparatedListMasker {

	/**
	 * Creates a masker for comma-separated PANs with default settings.
	 */
	public CommaSeparatedPanListMasker() {
		super(new PanMasker());
	}

	/**
	 * Creates a masker for comma-separated PANs with custom masking characters.
	 *
	 * @param maskingCharacters the masking characters
	 */
	public CommaSeparatedPanListMasker(String maskingCharacters) {
		super(new PanMasker(maskingCharacters));
	}

	/**
	 * Creates a masker from a {@link Mask} annotation.
	 *
	 * @param mask the mask annotation
	 */
	public CommaSeparatedPanListMasker(Mask mask) {
		this(mask.maskingChars());
	}
}
