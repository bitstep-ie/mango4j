package ie.bitstep.mango.validation.validators.annotations;

import ie.bitstep.mango.validation.validators.IdentifierValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DottedCaseValidator implements ConstraintValidator<IsValidDottedCase, String> {

	/**
	 * Initializes the validator.
	 *
	 * @param parameters the constraint annotation instance
	 */
	@Override
	public void initialize(IsValidDottedCase parameters) {
		// Do nothing
	}

	/**
	 * Validates the supplied string as dotted-case.
	 *
	 * @param value the value to validate
	 * @param constraintValidatorContext the validator context
	 * @return true when valid or null, false otherwise
	 */
	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		return IdentifierValidator.isValidDottedCase(value);
	}
}
