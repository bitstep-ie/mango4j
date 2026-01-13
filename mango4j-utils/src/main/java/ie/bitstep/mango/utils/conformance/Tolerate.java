package ie.bitstep.mango.utils.conformance;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Tolerate {
	// depends on what the data types is on what max means
	// String - string cannot be longer than
	// Numeric - value cannot be greater than
	// Anything else throws InvalidType
	long max();

	// depends on what the data types is on what max means
	// String - string cannot be shorter than
	// Numeric - value cannot be less than
	// Anything else throws InvalidType
	long min();
}
