package ie.bitstep.mango.reflection.utils;

import ie.bitstep.mango.reflection.accessors.Accessor;

public class AccessorsPrivateField {
	@Accessor(getter = "getMessage", setter = "setMessage")
	private String s;

	public void dummy() {
		// NOSONAR: Test class support
	}

	public void setMessage(String message) {
		this.s = message;
	}

	public String getMessage() {
		return s;
	}
}
