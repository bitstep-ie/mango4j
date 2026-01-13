package ie.bitstep.mango.crypto.domain;

import java.util.Collection;
import java.util.List;

/**
 * See javadocs for partner interface {@link Lookup}
 */
public interface Unique {
	void setUniqueValues(Collection<CryptoShieldHmacHolder> hmacHolders);

	List<CryptoShieldHmacHolder> getUniqueValues();
}
