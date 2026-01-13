package ie.bitstep.mango.utils.converters;


import org.apache.commons.collections4.CollectionUtils;

import java.util.function.UnaryOperator;

/**
 * Given a collection return it's size or null if the input is null
 */
public class CollectionToSize implements UnaryOperator<Object> {

	/**
	 * @param o the collection
	 * @return the size of the provided collection. Null if the collection is null to distinguish null collections from empty collections
	 */
	@Override
	public Integer apply(Object o) {
		// for logging, it might be useful to distinguish null collections from empty collections
		return o == null ? null : CollectionUtils.size(o);
	}

}