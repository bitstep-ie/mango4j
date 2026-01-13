package ie.bitstep.mango.utils.masking;

/**
 * Created by e048222 on 5/19/2017.
 */
public class CommaSeparatedPanListMasker extends AbstractCommaSeparatedListMasker {

	public CommaSeparatedPanListMasker() {
		super(new PanMasker());
	}

	public CommaSeparatedPanListMasker(String maskingCharacters) {
		super(new PanMasker(maskingCharacters));
	}

	public CommaSeparatedPanListMasker(Mask mask) {
		this(mask.maskingChars());
	}
}
