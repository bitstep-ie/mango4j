package ie.bitstep.mango.validation.validators;

import org.apache.commons.lang3.StringUtils;

public class IdentifierValidator {
	private static final String VALID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
	private static final String DASH = "-";
	private static final String DOT = ".";

	private IdentifierValidator() { // NOSONAR
	}

	public static boolean isValidKebabCase(String ident) {
		return isValidIdent(ident, VALID_CHARS, DASH);
	}

	public static boolean isValidDottedCase(String ident) {
		return isValidIdent(ident, VALID_CHARS, DOT);
	}

	private static boolean isValidIdent(String ident, String validChars, String separator) {
		return
			ident == null || (
			StringUtils.containsOnly(ident, validChars + separator) &&
			!StringUtils.startsWith(ident, separator) &&
			!StringUtils.endsWith(ident, separator) &&
			!StringUtils.contains(ident, separator + separator));
	}
}
