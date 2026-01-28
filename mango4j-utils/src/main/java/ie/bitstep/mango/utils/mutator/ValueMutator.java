package ie.bitstep.mango.utils.mutator;

import java.lang.annotation.Annotation;

public interface ValueMutator {
	/**
	 * Processes a value based on an annotation.
	 *
	 * @param a the annotation triggering this mutator
	 * @param in the input value
	 * @return the mutated value
	 * @throws IllegalArgumentException when the input type is unsupported
	 */
	Object process(Annotation a, Object in) throws IllegalArgumentException;
}
