package ie.bitstep.mango.crypto.keyrotation;

import ie.bitstep.mango.crypto.core.domain.CryptoKey;

import java.util.List;

public interface RekeyService<T> {

	Class<T> getEntityType();

	List<T> findRecordsNotUsingCryptoKey(CryptoKey cryptoKey);

	List<T> findRecordsUsingCryptoKey(CryptoKey cryptoKey);

	void save(List<?> records);

	void notify(ProgressTracker progressTracker);

	void purgeRedundantHmacs(CryptoKey cryptoKey);
}