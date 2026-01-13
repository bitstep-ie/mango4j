package ie.bitstep.mango.validation.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import java.util.Set;

public class ValidationUtilsException extends ValidationException {
	private final transient Set<ConstraintViolation<Object>> violations;

	public <T> ValidationUtilsException(Set<ConstraintViolation<Object>> violations) {
		this.violations = violations;
	}

	public Set<ConstraintViolation<Object>> getViolations() {
		return violations;
	}
}
