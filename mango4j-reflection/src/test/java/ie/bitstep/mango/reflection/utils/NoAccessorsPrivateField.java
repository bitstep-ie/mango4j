package ie.bitstep.mango.reflection.utils;

import ie.bitstep.mango.reflection.accessors.Accessor;

public class NoAccessorsPrivateField {
	@Accessor // No getter/setter specified
	private String s;

	public void dummy() {

	}

	public void setMessage(String message) {
		this.s = message;
	}

	public String getMessage() {
		return s;
	}
}
