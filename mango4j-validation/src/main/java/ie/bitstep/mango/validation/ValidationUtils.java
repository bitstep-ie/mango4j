package ie.bitstep.mango.validation;

import ie.bitstep.mango.validation.exceptions.ValidationUtilsException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

public class ValidationUtils {
	/**
	 * Prevents instantiation.
	 */
	private ValidationUtils() {
		// SONAR
	}

	/**
	 * Validates the supplied instance and throws {@link ValidationUtilsException} if violations exist.
	 *
	 * @param thing the object to validate
	 * @param <T> the object type
	 */
	public static <T> void validate(T thing) {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			Validator validator = factory.getValidator();
			Set<ConstraintViolation<Object>> violations = validator.validate(thing);

			if (!violations.isEmpty()) {
				throw new ValidationUtilsException(violations);
			}
		}
	}
}
