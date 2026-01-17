package ie.bitstep.mango.utils.converters;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

/**
 * Singleton converter factory.
 */
public final class ConverterFactory {

	private static final Map<Class<? extends UnaryOperator<Object>>, UnaryOperator<Object>> CONVERTERS_BY_CLASS = new ConcurrentHashMap<>();

	/**
	 * @param converterClass the class used to convert this
	 * @param o the object to convert
	 * @return the converted value
	 */
	public static Object convert(Class<? extends UnaryOperator<Object>> converterClass, Object o) {
		return getConverter(converterClass).apply(o);
	}

	/**
	 * @param converterClass the class for the converter
	 * @return the converter
	 */
	public static UnaryOperator<Object> getConverter(Class<? extends UnaryOperator<Object>> converterClass) {
		return CONVERTERS_BY_CLASS.computeIfAbsent(converterClass, ConverterFactory::createConverter);
	}

	/**
	 * @param converterClass the class for the converter
	 * @return the newly created converter
	 */
	static UnaryOperator<Object> createConverter(Class<? extends UnaryOperator<Object>> converterClass) {
		try {
			return converterClass.getConstructor().newInstance();
		} catch (IllegalAccessException | InstantiationException | NoSuchMethodException |
				 InvocationTargetException ex) {
			throw new IllegalStateException(ex); // must be able to instantiate
		}
	}

	/**
	 * Prevents instantiation.
	 */
	private ConverterFactory() {
		// NOSONAR
	}
}
