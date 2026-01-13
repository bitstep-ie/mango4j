package ie.bitstep.mango.utils.conformance;

import ie.bitstep.mango.utils.mutator.ObjectMutator;
import ie.bitstep.mango.utils.mutator.exceptions.MutatorException;

public class Conformer extends ObjectMutator {
	private Conformer() { // NOSONAR
	}

	public static void conform(Object object) throws MutatorException {
			new ObjectMutator()
				.on(Reduce.class, new ReduceMutator())
				.on(Tolerate.class, new TolerateMutator())
				.shallow()
				.mutate(object);
	}

	public static void deepConform(Object object) throws MutatorException {
			new ObjectMutator()
				.on(Reduce.class, new ReduceMutator())
				.on(Tolerate.class, new TolerateMutator())
				.deep()
				.mutate(object);
	}
}
