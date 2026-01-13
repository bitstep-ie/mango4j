package ie.bitstep.mango.utils.exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InvalidDateFormatException extends Exception {
	private final List<String> formats;

	public InvalidDateFormatException(Map<String, String> formats) {
		super("Invalid date format");
		this.formats = new ArrayList<>(formats.values());
	}

	public List<String> getFormats() {
		return formats;
	}
}
