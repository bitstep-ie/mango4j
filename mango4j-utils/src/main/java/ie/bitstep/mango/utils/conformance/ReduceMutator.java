package ie.bitstep.mango.utils.conformance;

import ie.bitstep.mango.utils.mutator.ValueMutator;

import java.lang.annotation.Annotation;

public class ReduceMutator implements ValueMutator {
	@Override
	public Object process(Annotation annotation, Object in) {
		Reduce reduce = (Reduce) annotation;
		if (in instanceof String s) {
			return reduce(reduce, s);
		} else {
			throw new IllegalArgumentException(in.getClass().getCanonicalName());
		}
	}

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

	int add(int in, int amount) {
		return in + amount;
	}

}
