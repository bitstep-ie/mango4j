package ie.bitstep.mango.crypto.keyrotation;

import ie.bitstep.mango.crypto.core.domain.CryptoKey;

import java.util.List;

public interface RekeyService<T> {

	/**
	 * Returns the entity type this service handles.
	 *
	 * @return the entity type
	 */
	Class<T> getEntityType();

	/**
	 * Finds records that do not use the supplied key.
	 *
	 * @param cryptoKey the crypto key
	 * @return records not using the key
	 */
	List<T> findRecordsNotUsingCryptoKey(CryptoKey cryptoKey);

	/**
	 * Finds records that use the supplied key.
	 *
	 * @param cryptoKey the crypto key
	 * @return records using the key
	 */
	List<T> findRecordsUsingCryptoKey(CryptoKey cryptoKey);

	/**
	 * Persists the supplied records.
	 *
	 * @param records the records to save
	 */
	void save(List<?> records);

	/**
	 * Receives progress updates for rekey operations.
	 *
	 * @param progressTracker the progress tracker
	 */
	void notify(ProgressTracker progressTracker);

	/**
	 * Purges redundant HMACs for the supplied key.
	 *
	 * @param cryptoKey the crypto key
	 */
	void purgeRedundantHmacs(CryptoKey cryptoKey);
}
