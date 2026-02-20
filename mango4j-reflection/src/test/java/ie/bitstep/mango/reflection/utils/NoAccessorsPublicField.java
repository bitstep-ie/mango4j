package ie.bitstep.mango.reflection.utils;

import ie.bitstep.mango.reflection.annotations.Modifier;

public class NoAccessorsPublicField {
	public String s;
	public static String staticS;

	@Modifier
	public void dummy() {
		// Only used for testing
	}

	public void message(String s) {
		// Only used for testing
	}
}
