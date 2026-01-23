package ie.bitstep.mango.utils.masking;

/**
 * A Masker that does nothing.
 */
public class NoMasker implements Masker {

	/**
	 * Returns the supplied value unchanged.
	 *
	 * @param value the input value
	 * @return the same value
	 */
	public String mask(String value) { return value; }
}
