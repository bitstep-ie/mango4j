package ie.bitstep.mango.utils.masking;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mask annotation, generic definition to allow a custom masking definition
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Mask {

	/**
	 * Specify the masking class, must have a constructor that takes a single parameter of type Mask
	 *
	 * @return the masker implementation class
	 */
	Class<? extends Masker> masker() default ParameterisedMasker.class;

	/**
	 * The character(s) to use for masking
	 *
	 * @return the masking characters
	 */
	String maskingChars() default "*";

	/**
	 * The prefix that should not be masked
	 *
	 * @return the prefix length
	 */
	int prefix() default 0;

	/**
	 * The postfix that should not be masked
	 *
	 * @return the postfix length
	 */
	int postfix() default 0;
}
