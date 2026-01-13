package ie.bitstep.mango.utils.conformance;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Reduce a string to a max length
 * Anything else throws InvalidType
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Reduce {
	// truncate a string to be no longer than max
	int max();

	// use ellipsis to indicate truncation
	boolean ellipsis() default true;
}
