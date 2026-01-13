package ie.bitstep.mango.utils.masking;

import org.apache.commons.lang3.StringUtils;

public final class MaskingUtils {

	public static final int MINIMUM_LENGTH_OF_MASKING_AFFIXES = 0;

	private MaskingUtils() { // NOSONAR
	}

	public static String mask(String value, String maskingCharacters, int maskPrefixLength, int maskSuffixLength) {
		if (StringUtils.isEmpty(value)) {
			return "";
		}

		validateAffixes(maskPrefixLength, maskSuffixLength);
		if (value.length() <= (maskPrefixLength + maskSuffixLength)) {
			// this forces the returned masked value to be all masked characters when the string isn't long enough
			maskPrefixLength = 0;
			maskSuffixLength = 0;
		}
		return buildMaskedValue(value, maskingCharacters, maskPrefixLength, maskSuffixLength);
	}

	private static void validateAffixes(int maskPrefixLength, int maskSuffixLength) {
		if (maskPrefixLength < MINIMUM_LENGTH_OF_MASKING_AFFIXES || maskSuffixLength < MINIMUM_LENGTH_OF_MASKING_AFFIXES) {
			throw new IllegalArgumentException(String.format("maskPrefixLength and maskSuffixLength must be greater than %s.Supplied values were %s and %s respectively",
					MINIMUM_LENGTH_OF_MASKING_AFFIXES, maskPrefixLength, maskSuffixLength));
		}
	}

	private static String buildMaskedValue(String value, String maskingCharacters, int maskPrefixLength, int maskSuffixLength) {
		StringBuilder maskedValueBuilder = new StringBuilder();
		for (int position = 0; position < value.length(); position++) {
			if (position < maskPrefixLength) {
				maskedValueBuilder.append(value.charAt(position));
			} else if (position < (value.length() - maskSuffixLength)) {
				maskedValueBuilder.append(maskingCharacters);
			} else {
				maskedValueBuilder.append(value.charAt(position));
			}
		}
		return maskedValueBuilder.toString();
	}
}
