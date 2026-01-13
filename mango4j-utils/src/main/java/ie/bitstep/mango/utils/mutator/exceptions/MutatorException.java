package ie.bitstep.mango.utils.mutator.exceptions;

public class MutatorException extends RuntimeException {
	public MutatorException(String message) {
		super(message);
	}
	public MutatorException(Exception e) {
		super(e);
	}
}
