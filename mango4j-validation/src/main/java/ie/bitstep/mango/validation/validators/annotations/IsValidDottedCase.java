package ie.bitstep.mango.validation.validators.annotations;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p> The annotated element must match the dotted.case naming style. </p>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = DottedCaseValidator.class)
public @interface IsValidDottedCase {
	/**
	 * Defines the default validation message.
	 *
	 * @return the message template
	 */
	String message() default "Value doesn't conform to dotted.case naming style";

	/**
	 * Defines the validation groups.
	 *
	 * @return the groups
	 */
	Class<?>[] groups() default {};

	/**
	 * Defines the payload for clients of the Bean Validation API.
	 *
	 * @return the payload
	 */
	Class<? extends Payload>[] payload() default {};
}
