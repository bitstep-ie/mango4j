package ie.bitstep.mango.crypto.core.factories;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurableObjectMapperFactory implements ObjectMapperFactory {
	public static final int ONE_KILOBYTE = 1024;
	private int maxStringLength = 20 * ONE_KILOBYTE;
	private ObjectMapper instance;

	public synchronized ObjectMapper objectMapper() {
		if (instance == null) {
			StreamReadConstraints constraints = StreamReadConstraints.builder()
				.maxStringLength(maxStringLength)
				.build();

			JsonFactory jsonFactory = JsonFactory.builder()
				.streamReadConstraints(constraints)
				.build();

			instance = new ObjectMapper(jsonFactory);
			instance.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		}

		return instance;
	}

	public synchronized ConfigurableObjectMapperFactory setMaxStringLength(int maxStringLength) {
		this.maxStringLength = maxStringLength;
		instance = null;
		return this;
	}
}

