package ie.bitstep.mango.crypto.hmac;

import ie.bitstep.mango.crypto.core.domain.CryptoKey;
import ie.bitstep.mango.crypto.core.domain.HmacHolder;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

public interface ListHmacFieldStrategyDelegate {
	List<CryptoKey> getCurrentHmacKeys();

	Collection<HmacHolder> getDefaultHmacHolders(List<CryptoKey> currentHmacKeys, Field sourceField, String fieldValue, Object entity);

	/**
	 * This method exists solely for performance benefits during a rekey task. If this is a rekey job, there's no point
	 * in calculating HMACs for {@link CryptoKey CryptoKeys} if HMACs with those same {@link CryptoKey CryptoKeys}
	 * already exist in the entity. This is because we know that the entity hasn't been updated if it's a rekey job.
	 * For normal operations, the source value could have been updated so even if HMACs with particular
	 * {@link CryptoKey CryptoKeys} already exist in the entity then we must recalculate them. Hence, for normal operations
	 * this method does nothing.
	 *
	 * @param entity
	 * @param lookupHmacs
	 * @param uniqueHmacs
	 */

	void preProcessForRekey(Object entity, List<HmacHolder> lookupHmacs, List<HmacHolder> uniqueHmacs);
}
