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
@Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$") // NOSONAR
@ReportAsSingleViolation
public @interface Type4UUID {
	String message() default "Lowercase type 4 like UUID required";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
