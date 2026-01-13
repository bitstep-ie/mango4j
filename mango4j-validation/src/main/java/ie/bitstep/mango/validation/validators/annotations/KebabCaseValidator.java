package ie.bitstep.mango.validation.validators.annotations;

import ie.bitstep.mango.validation.validators.IdentifierValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class KebabCaseValidator implements ConstraintValidator<IsValidKebabCase, String> {

	@Override
	public void initialize(IsValidKebabCase parameters) {
		// Do nothing
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
			return IdentifierValidator.isValidKebabCase(value);
	}
}
