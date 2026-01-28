package ie.bitstep.mango.validation.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import java.util.Set;

public class ValidationUtilsException extends ValidationException {
	private final transient Set<ConstraintViolation<Object>> violations;

	/**
	 * Creates an exception containing the validation violations.
	 *
	 * @param violations the constraint violations that triggered this exception
	 * @param <T> the validated type
	 */
	public <T> ValidationUtilsException(Set<ConstraintViolation<Object>> violations) {
		this.violations = violations;
	}

	/**
	 * Returns the violations that triggered this exception.
	 *
	 * @return the constraint violations
	 */
	public Set<ConstraintViolation<Object>> getViolations() {
		return violations;
	}
}
