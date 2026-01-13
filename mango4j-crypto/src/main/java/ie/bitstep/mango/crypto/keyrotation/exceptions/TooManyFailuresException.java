package ie.bitstep.mango.crypto.keyrotation.exceptions;

public class TooManyFailuresException extends RuntimeException {

	public TooManyFailuresException(String message) {
		super(message);
	}
}
