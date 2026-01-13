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

	private MDCTestUtils() {
		// SONAR
	}

	public static void init() {
		for (String fieldName : MDC_ADAPTER) {
			try {
				ReflectionUtils.forceSetField(MDC.class, fieldName, new BasicMDCAdapter());
			} catch (Exception e) {
				// NOSONAR: Field does not exist
			}
		}
	}

	public static void cleanup() {
		for (String fieldName : MDC_ADAPTER) {
			try {
				ReflectionUtils.forceSetField(MDC.class, fieldName, null);
			} catch (Exception e) {
				// NOSONAR: Field does not exist
			}
		}
	}

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
