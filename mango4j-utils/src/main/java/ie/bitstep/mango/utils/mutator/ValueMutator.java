package ie.bitstep.mango.utils.mutator;

import java.lang.annotation.Annotation;

public interface ValueMutator {
	Object process(Annotation a, Object in) throws IllegalArgumentException;
}
