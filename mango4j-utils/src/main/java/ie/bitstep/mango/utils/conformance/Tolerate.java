package ie.bitstep.mango.utils.conformance;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Tolerate {
	/**
	 * Defines the maximum tolerated value or length.
	 *
	 * @return the maximum value or length
	 */
	long max();

	/**
	 * Defines the minimum tolerated value or length.
	 *
	 * @return the minimum value or length
	 */
	long min();
}
