package ie.bitstep.mango.reflection.utils.test;

import ie.bitstep.mango.reflection.utils.ReflectionUtils;
import org.slf4j.MDC;
import org.slf4j.helpers.BasicMDCAdapter;

public class MDCTestUtils {
	private static final String[] MDC_ADAPTER =
		{
			"MDC_ADAPTER", // new name
			"mdcAdapter" // old name
		};

	/**
	 * Prevents instantiation.
	 */
	private MDCTestUtils() {
		// SONAR
	}

	/**
	 * Initializes the MDC adapter for tests, supporting multiple field names.
	 */
	public static void init() {
		for (String fieldName : MDC_ADAPTER) {
			try {
				ReflectionUtils.forceSetField(MDC.class, fieldName, new BasicMDCAdapter());
			} catch (Exception e) {
				// NOSONAR: Field does not exist
			}
		}
	}

	/**
	 * Clears the MDC adapter for tests.
	 */
	public static void cleanup() {
		for (String fieldName : MDC_ADAPTER) {
			try {
				ReflectionUtils.forceSetField(MDC.class, fieldName, null);
			} catch (Exception e) {
				// NOSONAR: Field does not exist
			}
		}
	}

	/**
	 * Returns the current MDC adapter instance, if available.
	 *
	 * @return the adapter or null
	 */
	public static Object getMDCAdapter() {
		Object response = null;

		for (String fieldName : MDC_ADAPTER) {
			try {
				Object tmp = ReflectionUtils.forceGetField(MDC.class, fieldName);

				if (tmp != null) {
					response = tmp;
				}
			} catch (Exception e) {
				// NOSONAR: Field does not exist
			}
		}

		return response;
	}
}
