package ie.bitstep.mango.validation.validators.annotations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DottedCaseValidatorTest {

	private final DottedCaseValidator validator = new DottedCaseValidator();

	@Mock
	ConstraintValidatorContext context;

	@Mock
	IsValidDottedCase isValidCase;

	@Test
	void isValidDottedCase() {
		validator.initialize(isValidCase);

		assertTrue(validator.isValid("dotted", context));
		assertTrue(validator.isValid("nice.dotted0", context));
		assertFalse(validator.isValid("Xnice.dotted", context));
		assertFalse(validator.isValid("!nice.dotted", context));
		assertFalse(validator.isValid("@nice.dotted", context));
		assertFalse(validator.isValid(".nice.dotted0", context));
		assertFalse(validator.isValid("nice.dotted0-", context));
		assertFalse(validator.isValid("nice..dotted0", context));
		assertFalse(validator.isValid("Nice.Dotted0", context));
	}

}
