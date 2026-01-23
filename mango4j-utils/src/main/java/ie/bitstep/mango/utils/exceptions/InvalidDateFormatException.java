package ie.bitstep.mango.utils.exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InvalidDateFormatException extends Exception {
	private final List<String> formats;

	/**
	 * Creates an exception listing supported date formats.
	 *
	 * @param formats supported format definitions
	 */
	public InvalidDateFormatException(Map<String, String> formats) {
		super("Invalid date format");
		this.formats = new ArrayList<>(formats.values());
	}

	/**
	 * Returns the supported formats.
	 *
	 * @return supported date formats
	 */
	public List<String> getFormats() {
		return formats;
	}
}
