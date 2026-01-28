package ie.bitstep.mango.utils.conformance;

import ie.bitstep.mango.utils.mutator.ValueMutator;

import java.lang.annotation.Annotation;

public class ReduceMutator implements ValueMutator {
	/**
	 * Applies reduction rules to the input value.
	 *
	 * @param annotation the annotation describing constraints
	 * @param in the input value
	 * @return the conformed value
	 */
	@Override
	public Object process(Annotation annotation, Object in) {
		Reduce reduce = (Reduce) annotation;
		if (in instanceof String s) {
			return reduce(reduce, s);
		} else {
			throw new IllegalArgumentException(in.getClass().getCanonicalName());
		}
	}

	/**
	 * Reduces a string to the configured maximum length.
	 *
	 * @param reduce the reduction annotation
	 * @param in the input string
	 * @return the reduced string
	 */
	private Object reduce(Reduce reduce, String in) {
		if (in.length() >= add(reduce.max(), 1)) {
			if (reduce.ellipsis() && reduce.max() >= add(3, 1)) {
				return in.substring(0, reduce.max() - 3) + "...";
			} else {
				return in.substring(0, reduce.max());
			}
		}

		return in;
	}

	/**
	 * Adds a delta to a numeric value.
	 *
	 * @param in the base value
	 * @param amount the delta to apply
	 * @return the new value
	 */
	int add(int in, int amount) {
		return in + amount;
	}

}
