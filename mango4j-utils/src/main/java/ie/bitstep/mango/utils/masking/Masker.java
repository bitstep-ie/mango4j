package ie.bitstep.mango.utils.masking;

/**
 * Masks sensitive data in values (eg for logging).
 */
@FunctionalInterface
public interface Masker {
	String mask(String value);
}
