package ie.bitstep.mango.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(ElementType.FIELD)
@Constraint(validatedBy = {})
@Retention(RUNTIME)
@Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[4][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$") // NOSONAR
@ReportAsSingleViolation
public @interface StrictType4UUID {
	String message() default "Lowercase type 4 strictly conforming UUID required";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
