package ie.bitstep.mango.validation.validators.annotations;

import ie.bitstep.mango.validation.validators.IdentifierValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DottedCaseValidator implements ConstraintValidator<IsValidDottedCase, String> {

	@Override
	public void initialize(IsValidDottedCase parameters) {
		// Do nothing
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		return IdentifierValidator.isValidDottedCase(value);
	}
}
