package ie.bitstep.mango.reflection.utils;

public class StringUtils {
	private StringUtils() { // SONAR
	}

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
