package ie.bitstep.mango.utils.masking;

/**
 * A Masker that does nothing.
 */
public class NoMasker implements Masker {

	public String mask(String value) { return value; }
}
