package ie.bitstep.mango.validation.validators;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

public class IdentifierValidator {
	private static final String VALID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
	private static final String DASH = "-";
	private static final String DOT = ".";

	/**
	 * Prevents instantiation.
	 */
	private IdentifierValidator() { // NOSONAR
	}

	/**
	 * Checks whether the identifier is valid kebab-case.
	 *
	 * @param ident the identifier to check
	 * @return true if valid or null, false otherwise
	 */
	public static boolean isValidKebabCase(String ident) {
		return isValidIdent(ident, VALID_CHARS, DASH);
	}

	/**
	 * Checks whether the identifier is valid dotted.case.
	 *
	 * @param ident the identifier to check
	 * @return true if valid or null, false otherwise
	 */
	public static boolean isValidDottedCase(String ident) {
		return isValidIdent(ident, VALID_CHARS, DOT);
	}

	/**
	 * Validates an identifier using allowed characters and separators.
	 *
	 * @param ident the identifier to check
	 * @param validChars allowed characters
	 * @param separator separator character to allow between segments
	 * @return true if valid or null, false otherwise
	 */
	private static boolean isValidIdent(String ident, String validChars, String separator) {
		return
			ident == null || (
			StringUtils.containsOnly(ident, validChars + separator) &&
			!Strings.CS.startsWith(ident, separator) &&
			!Strings.CS.endsWith(ident, separator) &&
			!Strings.CS.contains(ident, separator + separator));
	}
}
