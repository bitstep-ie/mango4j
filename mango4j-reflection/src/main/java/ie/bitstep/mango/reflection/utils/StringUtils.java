package ie.bitstep.mango.reflection.utils;

public class StringUtils {
	/**
	 * Prevents instantiation.
	 */
	private StringUtils() { // SONAR
	}

	/**
	 * Capitalizes the first character of the supplied string.
	 *
	 * @param s the input string
	 * @return the capitalized string, or the original when capitalization fails
	 */
	public static String capitalize(String s) {
		try {
			return s.substring(0, 1).toUpperCase() + s.substring(1);
		}
		catch (Exception e) {
			// failed, return original string
			return s;
		}
	}
}
