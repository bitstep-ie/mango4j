package ie.bitstep.mango.utils.mapping;

import ie.bitstep.mango.utils.mapping.exceptions.MappingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class Profile {
	private String firstName;
	private String lastName;

	public String getFirstName() {
		return firstName;
	}

	public Profile setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getLastName() {
		return lastName;
	}

	public Profile setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}
}

class BadClass {
	public String toString() {
		throw new RuntimeException("deliberate throw!");
	}
}

class MappingUtilsTest {

	@Test
	void constructor() throws Exception {
		Constructor<MappingUtils> constructor = MappingUtils.class.getDeclaredConstructor();
		constructor.setAccessible(true);

		assertThat(constructor.newInstance()).isInstanceOf(MappingUtils.class);
	}

	@Test
	void fromObjectToMap() {
		Profile p = new Profile();
		p.setFirstName("Stephen");
		p.setLastName("Hawking");

		Map<String, Object> result = MappingUtils.fromObjectToMap(p);

		assertThat(result)
				.containsEntry("firstName", p.getFirstName())
				.containsEntry("lastName", p.getLastName());
	}

	@Test
	void fromObjectToMapFailure() {
		BadClass badClass = new BadClass();

		MappingException thrown = Assertions.assertThrows(MappingException.class, () -> {
			MappingUtils.fromObjectToMap(badClass);
		});

		assertThat(thrown.getMessage()).contains("com.fasterxml.jackson.databind.exc.InvalidDefinitionException");
	}

	@Test
	void fromObjectToJson() {
		Profile p = new Profile();
		p.setFirstName("Stephen");
		p.setLastName("Hawking");

		String result = MappingUtils.fromObjectToJson(p);

		assertThat(result).isEqualTo("{\"firstName\":\"Stephen\",\"lastName\":\"Hawking\"}");
	}

	@Test
	void fromObjectToJsonFailure() {
		BadClass badClass = new BadClass();

		MappingException thrown = Assertions.assertThrows(MappingException.class, () -> {
			MappingUtils.fromObjectToJson(badClass);
		});

		assertThat(thrown.getMessage()).contains("com.fasterxml.jackson.databind.exc.InvalidDefinitionException");
	}

	@Test
	void fromJsonToMap() {
		Profile p = new Profile();
		p.setFirstName("Stephen");
		p.setLastName("Hawking");

		String json = MappingUtils.fromObjectToJson(p);
		Map<String, Object> result = MappingUtils.fromJsonToMap(json);

		assertThat(result)
				.containsEntry("firstName", p.getFirstName())
				.containsEntry("lastName", p.getLastName());
	}

	@Test
	void fromJsonFailure() {
		MappingException thrown = Assertions.assertThrows(MappingException.class, () -> {
			MappingUtils.fromJsonToMap("<>");
		});

		assertThat(thrown.getMessage()).contains("com.fasterxml.jackson.core.JsonParseException");
	}

	@Test
	void fromEmptyJsonToMap() {
		Map<String, Object> result = MappingUtils.fromJsonToMap("");

		assertThat(result).isInstanceOf(LinkedHashMap.class).isEmpty();
	}
}
