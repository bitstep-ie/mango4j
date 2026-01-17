package ie.bitstep.mango.utils.mutator.exceptions;

public class MutatorException extends RuntimeException {
	/**
	 * Creates an exception with a message.
	 *
	 * @param message the error message
	 */
	public MutatorException(String message) {
		super(message);
	}
	/**
	 * Creates an exception with a cause.
	 *
	 * @param e the underlying exception
	 */
	public MutatorException(Exception e) {
		super(e);
	}
}
