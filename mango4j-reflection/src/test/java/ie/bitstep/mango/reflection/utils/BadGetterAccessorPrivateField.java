package ie.bitstep.mango.reflection.utils;

import ie.bitstep.mango.reflection.accessors.Accessor;

public class BadGetterAccessorPrivateField {
	@Accessor(getter = "getMessage")
	private String s;

	public void dummy() {
		// Only used for testing the accessor annotation with a non-existent getter method.
	}
}
