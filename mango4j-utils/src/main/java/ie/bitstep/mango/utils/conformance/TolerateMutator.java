package ie.bitstep.mango.utils.conformance;

import ie.bitstep.mango.utils.mutator.ValueMutator;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;

public class TolerateMutator implements ValueMutator {
	@Override
	public Object process(Annotation annotation, Object in) {
		return tolerate((Tolerate) annotation, in);
	}

	Object tolerate(Tolerate tolerate, Object in) {
		if (in instanceof String value) {
			return tolerateString(tolerate, value);
		} else if (in instanceof Long value) {
			return tolerateLong(tolerate, value);
		} else if (in instanceof Integer value) {
			return tolerateInteger(tolerate, value);
		} else {
			throw new IllegalArgumentException(in.getClass().getCanonicalName());
		}
	}

	private long tolerateLong(Tolerate tolerate, Long in) {
		if (in.longValue() >= add(tolerate.max(), 1)) {
			return tolerate.max();
		} else if (in.longValue() <= add(tolerate.min(), -1)) {
			return tolerate.min();
		}

		return in;
	}

	private int tolerateInteger(Tolerate tolerate, Integer in) {
		if (in.intValue() >= add(tolerate.max(), 1)) {
			return (int) tolerate.max();
		} else if (in.intValue() <= add(tolerate.min(), -1)) {
			return (int) tolerate.min();
		}

		return in;
	}

	Object tolerateString(Tolerate tolerate, String in) {
		if (in.length() >= add(tolerate.max(), 1)) {
			return in.substring(0, (int) tolerate.max());
		} else if (in.length() <= add(tolerate.min(), -1)) {
			return StringUtils.rightPad(in, (int) tolerate.min());
		}

		return in;
	}

	long add(long in, long amount) {
		return in + amount;
	}
}
