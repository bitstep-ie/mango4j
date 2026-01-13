package ie.bitstep.mango.validation;

import ie.bitstep.mango.validation.constraints.StrictType4UUID;
import ie.bitstep.mango.validation.constraints.Type4UUID;
import ie.bitstep.mango.validation.exceptions.ValidationUtilsException;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class Entity {
	@Size(max = 255)
	@Type4UUID
	private final String id1;

	@Size(max = 255)
	@StrictType4UUID
	private final String id2;

	public Entity(String id1, String id2) {
		this.id1 = id1;
		this.id2 = id2;
	}
}

class ValidationUtilsTest {
	@Test
	void validateEntityFail() {
		Entity e = new Entity("hello", "1a7b7d29-f25f-13f8-0cc7-98eebb96e8af");

		ValidationUtilsException thrown = Assertions.assertThrows(ValidationUtilsException.class, () -> ValidationUtils.validate(e));
		assertThat(thrown.getViolations()).hasSize(2);

		thrown.getViolations().forEach(v -> {
			assertThat(v.getPropertyPath().toString()).startsWith("id");
			assertThat(v.getRootBean()).isEqualTo(e);
		});
	}

	@Test
	void validateEntitySucceed() {
		Entity e = new Entity(UUID.randomUUID().toString(), UUID.randomUUID().toString());

		Assertions.assertDoesNotThrow(() -> ValidationUtils.validate(e));
	}
}
