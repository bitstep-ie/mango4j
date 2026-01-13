package ie.bitstep.mango.utils.masking;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCommaSeparatedListMasker implements Masker {

	private static final String VALUE_SEPARATOR_CHARACTER = ",";

	protected final Masker masker;

	protected AbstractCommaSeparatedListMasker(Masker masker) {
		this.masker = masker;
	}

	@Override
	public String mask(String input) {
		String[] unmaskedValues = input.split(VALUE_SEPARATOR_CHARACTER);
		List<String> maskedValues = new ArrayList<>();
		for (String unmaskedValue: unmaskedValues) {
			maskedValues.add(masker.mask(unmaskedValue));
		}
		return StringUtils.join(maskedValues, VALUE_SEPARATOR_CHARACTER);
	}
}
