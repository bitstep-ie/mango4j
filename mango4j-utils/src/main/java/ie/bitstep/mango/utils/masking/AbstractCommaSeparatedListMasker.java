package ie.bitstep.mango.utils.masking;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCommaSeparatedListMasker implements Masker {

	private static final String VALUE_SEPARATOR_CHARACTER = ",";

	protected final Masker masker;

	/**
	 * Creates a masker for comma-separated values using the supplied masker.
	 *
	 * @param masker the masker to apply to each value
	 */
	protected AbstractCommaSeparatedListMasker(Masker masker) {
		this.masker = masker;
	}

	/**
	 * Masks a comma-separated list using the configured masker.
	 *
	 * @param input the input string
	 * @return the masked list
	 */
	@Override
	public String mask(String input) {
		String[] unmaskedValues = input.split(VALUE_SEPARATOR_CHARACTER);
		List<String> maskedValues = new ArrayList<>();
		for (String unmaskedValue : unmaskedValues) {
			maskedValues.add(masker.mask(unmaskedValue));
		}
		return StringUtils.join(maskedValues, VALUE_SEPARATOR_CHARACTER);
	}
}
