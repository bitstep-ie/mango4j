package ie.bitstep.mango.utils.masking;

/**
 * Created by e048222 on 5/19/2017.
 */
public class CommaSeparatedIdListMasker extends AbstractCommaSeparatedListMasker {

	public CommaSeparatedIdListMasker() {
		super(new IdMasker());
	}

	public CommaSeparatedIdListMasker(String maskingCharacters) {
		super(new IdMasker(maskingCharacters));
	}

	public CommaSeparatedIdListMasker(Mask mask) {
		this(mask.maskingChars());
	}
}
