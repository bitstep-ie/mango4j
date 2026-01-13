package ie.bitstep.mango.validation.validators.annotations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class KebabCaseValidatorTest {

	private final KebabCaseValidator validator = new KebabCaseValidator();

	@Mock
	ConstraintValidatorContext context;

	@Mock
	IsValidKebabCase isValidCase;

	@Test
	void isValidKebabCase() {

		validator.initialize(isValidCase);

		assertTrue(validator.isValid("abc", context));
		assertFalse(validator.isValid("Xnice-kebab", context));
		assertFalse(validator.isValid("!nice-kebab", context));
		assertFalse(validator.isValid("@nice-kebab", context));
		assertFalse(validator.isValid("-nice-kebab0", context));
		assertFalse(validator.isValid("nice-kebab0-", context));
		assertFalse(validator.isValid("nice--kebab0", context));
		assertFalse(validator.isValid("Nice-Kebab0", context));
	}

}
