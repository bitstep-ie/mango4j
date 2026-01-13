package ie.bitstep.mango.utils.converters;

import java.util.function.UnaryOperator;

/**
 * The do nothing converter, returns the same object it is passed
 */
public class NoConverter implements UnaryOperator<Object> {

	/**
	 * @param o the object
	 * @return the input object
	 */
	@Override
	public Object apply(Object o) {
		return o;
	}
}