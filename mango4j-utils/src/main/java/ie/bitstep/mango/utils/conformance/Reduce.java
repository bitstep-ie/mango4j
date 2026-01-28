package ie.bitstep.mango.utils.conformance;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Reduce a string to a max length
 * Anything else throws InvalidType
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Reduce {
	/**
	 * Truncates a string to be no longer than max.
	 *
	 * @return the maximum length
	 */
	int max();

	/**
	 * Uses ellipsis to indicate truncation.
	 *
	 * @return true to append ellipsis
	 */
	boolean ellipsis() default true;
}
