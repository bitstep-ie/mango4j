package ie.bitstep.mango.validation.validators;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentifierValidatorTest {
	@Test
	void isValidKebabCase() {
		assertTrue(IdentifierValidator.isValidKebabCase(null));
		assertTrue(IdentifierValidator.isValidKebabCase("nice-kebab0"));
		assertFalse(IdentifierValidator.isValidKebabCase("Xnice-kebab"));
		assertFalse(IdentifierValidator.isValidKebabCase("!nice-kebab"));
		assertFalse(IdentifierValidator.isValidKebabCase("@nice-kebab"));
		assertFalse(IdentifierValidator.isValidKebabCase("-nice-kebab0"));
		assertFalse(IdentifierValidator.isValidKebabCase("nice-kebab0-"));
		assertFalse(IdentifierValidator.isValidKebabCase("nice--kebab0"));
		assertFalse(IdentifierValidator.isValidKebabCase("Nice-Kebab0"));
	}

	@Test
	void isValidDottedCase() {
		assertTrue(IdentifierValidator.isValidDottedCase(null));
		assertTrue(IdentifierValidator.isValidDottedCase("nice.dotted0"));
		assertFalse(IdentifierValidator.isValidDottedCase("Xnice.dotted"));
		assertFalse(IdentifierValidator.isValidDottedCase("!nice.dotted"));
		assertFalse(IdentifierValidator.isValidDottedCase("@nice.dotted"));
		assertFalse(IdentifierValidator.isValidDottedCase(".nice.dotted0"));
		assertFalse(IdentifierValidator.isValidDottedCase("nice.dotted0-"));
		assertFalse(IdentifierValidator.isValidDottedCase("nice..dotted0"));
		assertFalse(IdentifierValidator.isValidDottedCase("Nice.Dotted0"));
	}
}
