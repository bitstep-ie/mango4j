package ie.bitstep.mango.utils.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ie.bitstep.mango.utils.mapping.exceptions.MappingException;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class MappingUtils {
	public static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {
	};

	/**
	 * Prevents instantiation.
	 */
	private MappingUtils() {
		// NOSONAR
	}

	/**
	 * Convert an object to a generic Map
	 * This is based on ability to serialize and deserialize into json the object to be converted
	 * json limitations apply (note the serialization and deserialization if the object to be converted has jackson annotations e.g: JsonIgnore, etc.)
	 *
	 * @param object the object to be mapped
	 * @return A generic map of representing the object
	 * @throws MappingException as runtime exception in case there is an issue processing JSON
	 */
	public static Map<String, Object> fromObjectToMap(Object object) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String json = objectMapper.writeValueAsString(object);
			return objectMapper.readValue(json, MAP_TYPE_REFERENCE);
		} catch (JsonProcessingException e) { // NOSONAR this should not happen at this stage, however gracefully handle it
			throw new MappingException(e);
		}
	}

	/**
	 * Convert an object to a JSON string
	 * @param object the object to be mapped
	 * @return A JSON string representation of the object
	 * @throws MappingException as runtime exception in case there is an issue processing JSON
	 */
	public static String fromObjectToJson(Object object) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) { // NOSONAR this should not happen at this stage, however gracefully handle it
			throw new MappingException(e);
		}
	}

	/**
	 * Convert a JSON string to a map
	 * @param json The JSON string
	 * @return The map equivalent of the JSON string
	 * @throws MappingException as runtime exception in case there is an issue processing JSON
	 */
	public static Map<String, Object> fromJsonToMap(String json) {
		try {
			if (StringUtils.isEmpty(json)) {
				return new LinkedHashMap<>();
			} else {
				ObjectMapper objectMapper = new ObjectMapper();
				return objectMapper.readValue(json, MAP_TYPE_REFERENCE);
			}
		} catch (JsonProcessingException e) { // NOSONAR this should not happen at this stage, however gracefully handle it
			throw new MappingException(e);
		}
	}
}
