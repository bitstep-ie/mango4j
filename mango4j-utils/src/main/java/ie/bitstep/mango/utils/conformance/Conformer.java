package ie.bitstep.mango.utils.conformance;

import ie.bitstep.mango.utils.mutator.ObjectMutator;
import ie.bitstep.mango.utils.mutator.exceptions.MutatorException;

public class Conformer extends ObjectMutator {
	/**
	 * Prevents instantiation.
	 */
	private Conformer() { // NOSONAR
	}

	/**
	 * Conforms the object's direct fields using {@link Reduce} and {@link Tolerate}.
	 *
	 * @param object the object to conform
	 * @throws MutatorException when mutation fails
	 */
	public static void conform(Object object) throws MutatorException {
			new ObjectMutator()
				.on(Reduce.class, new ReduceMutator())
				.on(Tolerate.class, new TolerateMutator())
				.shallow()
				.mutate(object);
	}

	/**
	 * Conforms the object and nested fields using {@link Reduce} and {@link Tolerate}.
	 *
	 * @param object the object to conform
	 * @throws MutatorException when mutation fails
	 */
	public static void deepConform(Object object) throws MutatorException {
			new ObjectMutator()
				.on(Reduce.class, new ReduceMutator())
				.on(Tolerate.class, new TolerateMutator())
				.deep()
				.mutate(object);
	}
}
