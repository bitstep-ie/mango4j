package ie.bitstep.mango.crypto.keyrotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.bitstep.mango.crypto.CryptoShield;
import ie.bitstep.mango.crypto.RekeyCryptoShield;
import ie.bitstep.mango.crypto.annotations.EncryptedBlob;
import ie.bitstep.mango.crypto.core.domain.CryptoKey;
import ie.bitstep.mango.crypto.core.domain.CryptoKeyUsage;
import ie.bitstep.mango.crypto.core.encryption.EncryptionService;
import ie.bitstep.mango.crypto.core.providers.CryptoKeyProvider;
import ie.bitstep.mango.crypto.keyrotation.exceptions.RekeySchedulerInitializationException;
import ie.bitstep.mango.crypto.keyrotation.exceptions.TooManyFailuresException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static ie.bitstep.mango.crypto.core.domain.CryptoKey.RekeyMode.KEY_OFF;
import static ie.bitstep.mango.crypto.core.domain.CryptoKey.RekeyMode.KEY_ON;
import static ie.bitstep.mango.crypto.keyrotation.RekeyEvent.Type.REKEY_FINISHED;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.Collections.emptyList;

/**
 * <b>Warning!!!</b> This class is currently experimental and is expected to go through several more iterations of both redesign
 * and refactoring. If you can read this message, don't use in production code just yet.
 */
@SuppressWarnings("unused")
public class RekeyScheduler {
	private static final String[] ORDINAL_SUFFIXES = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};

	private final System.Logger logger = System.getLogger(RekeyScheduler.class.getName());

	private Collection<RekeyService<?>> rekeyServices;
	private ObjectMapper objectMapper;
	private Clock clock;
	private int initialDelay;
	private Duration cryptoKeyCacheDuration;
	private Duration batchInterval = Duration.ZERO;
	private int maximumToleratedFailuresPerExecution = -1;
	private RekeyCryptoKeyManager rekeyCryptoKeyManager;
	private int rekeyCheckInterval;
	private TimeUnit rekeyTimeUnits;
	private CryptoShield cryptoShield;

	public RekeyScheduler() {
	}

	/**
	 * Mandatory method. The usual {@link CryptoKeyProvider} implementation used by your application
	 *
	 * @return this
	 */
	public RekeyScheduler withCryptoShield(CryptoShield cryptoShield) {
		this.cryptoShield = cryptoShield;
		return this;
	}

	/**
	 * Mandatory method: Sets all the application's {@link RekeyService} implementations that this scheduler will use to rekey records
	 *
	 * @param rekeyServices All {@link RekeyService} implementations for the application. There should be 1 {@link RekeyService}
	 *                      per entity that uses encryption.
	 * @return this
	 */
	public RekeyScheduler withRekeyServices(Collection<RekeyService<?>> rekeyServices) {
		this.rekeyServices = rekeyServices;
		return this;
	}

	/**
	 * Mandatory method: Sets the {@link RekeyCryptoKeyManager} implementation that this scheduler will use to delete keys that are
	 * no longer in use.
	 *
	 * @param rekeyCryptoKeyManager {@link RekeyCryptoKeyManager} implementation for the application.
	 * @return this
	 */
	public RekeyScheduler withRekeyCryptoManager(RekeyCryptoKeyManager rekeyCryptoKeyManager) {
		this.rekeyCryptoKeyManager = rekeyCryptoKeyManager;
		return this;
	}

	/**
	 * Mandatory method: The {@link ObjectMapper} to use for generating the final ciphertext for
	 * &#64;{@link EncryptedBlob EncryptedBlob} fields.
	 * This should be the same as the one supplied to {@link CryptoShield}.
	 * We need it to be supplied here also because internally this class instantiates new {@link CryptoShield}
	 * instances for each rekey.
	 *
	 * @param objectMapper {@link ObjectMapper} implementation to use for
	 *                     &#64;{@link EncryptedBlob EncryptedBlob} ciphertext formatting
	 * @return this
	 */
	public RekeyScheduler withObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		return this;
	}

	/**
	 * Mandatory method
	 *
	 * @param clock clock instance to use.
	 * @return this
	 */
	public RekeyScheduler withClock(Clock clock) {
		this.clock = clock;
		return this;
	}

	/**
	 * Mandatory method: This is an extremely important field to set and applications must make sure to set it to the correct value.
	 * Failure to set this to the correct value may have negative consequences for application functionality during a rekey job.
	 * If your application is multi-instance and caches {@link CryptoKey} data for performance reasons (very common) then
	 * there is a period of time after a new key is created in the system before all instances know about it. If a
	 * rekey job kicked off before this period then it would start to rekey encrypted data/HMACs to a key that some
	 * instances don't know about. In the case of HMACs this will result in search misses and possibly the more
	 * serious duplicate values problem (if HMACs are used to enforce uniqueness).
	 *
	 * @param cryptoKeyCacheDuration The length of time your application instances cache {@link CryptoKey CryptoKeys}
	 *                               for (if applicable). If not applicable then just set it to {@link Duration#ZERO}. Cannot be null.
	 * @return this
	 */
	public RekeyScheduler withCryptoKeyCachePeriod(Duration cryptoKeyCacheDuration) {
		this.cryptoKeyCacheDuration = cryptoKeyCacheDuration;
		return this;
	}

	/**
	 * Optional method: To avoid overwhelming the application database and {@link EncryptionService}
	 * implementations, you can set this field to some duration. After each batch of records is re-keyed this library
	 * will sleep for this length of time before it asks {@link RekeyService#findRecordsNotUsingCryptoKey(CryptoKey)}
	 * or {@link RekeyService#findRecordsUsingCryptoKey(CryptoKey)} for another batch of records to rekey.
	 *
	 * @param batchInterval The amount of time to sleep after a batch of records is re-keyed.
	 * @return this
	 */
	public RekeyScheduler withBatchInterval(Duration batchInterval) {
		this.batchInterval = batchInterval;
		return this;
	}

	/**
	 * Optional method: You can set a maximum value for failures for each rekey job that gets kicked off after which the
	 * job will abort. So if this class encounters some problems decrypting, re-encrypting or saving records then this job will abort
	 * this run of the process.
	 *
	 * @param maximumToleratedFailuresPerExecution The number of rekey failures that will trigger this library to abort
	 *                                             the rekey job (per tenant if applicable)
	 * @return this
	 */
	public RekeyScheduler withMaximumToleratedFailuresPerExecution(int maximumToleratedFailuresPerExecution) {
		this.maximumToleratedFailuresPerExecution = maximumToleratedFailuresPerExecution;
		return this;
	}

	/**
	 * Mandatory method: This specifies the scheduling settings for this rekey Scheduler.
	 *
	 * @param initialDelay       The time after the {@link RekeyScheduler#start()} method is called which you want to wait before the first rekey job begins.
	 * @param rekeyCheckInterval The period of time between subsequent rekey jobs. Since rekey operations are usually
	 *                           quite rare setting this to once a day is probably adequate. This scheduler will wake up and check for any pending
	 *                           KEY_ON/KEY_OFF jobs (as signalled by the {@link CryptoKey#rekeyMode}) field.
	 * @param rekeyTimeUnits     The time units that initialDelay and rekeyCheckInterval parameters are specified in.
	 * @return this
	 */
	public RekeyScheduler withRekeyCheckInterval(int initialDelay, int rekeyCheckInterval, TimeUnit rekeyTimeUnits) {
		this.initialDelay = initialDelay;
		this.rekeyCheckInterval = rekeyCheckInterval;
		this.rekeyTimeUnits = rekeyTimeUnits;
		return this;
	}

	/**
	 * Schedules a reoccurring rekey job as per the specified settings.
	 */
	public void start() {
		validateSettings();
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(this::rekeyTenants, initialDelay, rekeyCheckInterval, rekeyTimeUnits);
	}

	private void validateSettings() {
		boolean isValid = areRekeyServicesValid()
				&& isObjectMapperValid()
				&& isClockValid()
				&& isCryptoKeyCacheDurationValid()
				&& isRekeyCryptoManagerValid()
				&& isRekeyCheckIntervalValid()
				&& areRekeyTimeUnitsValid()
				&& isBatchIntervalValid();

		if (!isValid) {
			throw new RekeySchedulerInitializationException();
		}
	}

	private boolean isBatchIntervalValid() {
		boolean isValid = true;
		if (batchInterval == null) {
			logger.log(ERROR, "batchInterval field was set to null. " +
					"Please make sure to set it to a non-null value using the withBatchInterval() method");
			isValid = false;
		}
		return isValid;
	}

	private boolean areRekeyTimeUnitsValid() {
		boolean isValid = true;
		if (rekeyTimeUnits == null) {
			logger.log(ERROR, "rekeyTimeUnits field was set to null. " +
					"Please make sure to set it to a non-null value using the withRekeyTimeUnits() method");
			isValid = false;
		}
		return isValid;
	}

	private boolean isRekeyCheckIntervalValid() {
		boolean isValid = true;
		if (rekeyCheckInterval == 0) {
			logger.log(ERROR, "rekeyCheckInterval field was set to 0. " +
					"Please make sure to set it to a positive non-zero integer value using the withRekeyCheckInterval() method");
			isValid = false;
		}
		return isValid;
	}

	private boolean isRekeyCryptoManagerValid() {
		boolean isValid = true;
		if (rekeyCryptoKeyManager == null) {
			logger.log(ERROR, "rekeyCryptoKeyManager field was set to a null value . " +
					"Please make sure to set it to a non-null value using the withRekeyCryptoKeyManager() method");
			isValid = false;
		}
		return isValid;
	}

	private boolean isCryptoKeyCacheDurationValid() {
		boolean isValid = true;
		if (cryptoKeyCacheDuration == null) {
			logger.log(ERROR, "cryptoKeyCacheDuration field was set to a null value . " +
					"Please make sure to set it to a non-null value using the withCryptoKeyCachePeriod() method");
			isValid = false;
		}
		return isValid;
	}

	private boolean isClockValid() {
		boolean isValid = true;
		if (clock == null) {
			logger.log(ERROR, "Clock field was set to a null value . " +
					"Please make sure to set it to a non-null value using the withClock() method");
			isValid = false;
		}
		return isValid;
	}

	private boolean isObjectMapperValid() {
		boolean isValid = true;
		if (objectMapper == null) {
			logger.log(ERROR, "ObjectMapper field was set to a null value . " +
					"Please make sure to set it to a non-null value using the withObjectMapper() method");
			isValid = false;
		}
		return isValid;
	}

	private boolean areRekeyServicesValid() {
		boolean isValid = true;
		if (rekeyServices == null || rekeyServices.isEmpty()) {
			logger.log(ERROR, "RekeyServices field was set to a{0} value . " +
					"Please make sure to set it to a non-empty collection using the withRekeyServices() method", rekeyServices == null ? " null" : "n empty");
			isValid = false;
		}
		return isValid;
	}

	private void rekeyTenants() {
		logger.log(TRACE, "Beginning rekey job");
		Map<String, List<CryptoKey>> allEncryptionKeys = new HashMap<>();
		for (CryptoKey cryptoKey : cryptoShield.getCryptoKeyProvider().getAllCryptoKeys()) {
			// HashMap allows null keys so if the app doesn't have tenants then cryptokey.getTenantId() can return null and
			// this functionality will still work fine
			allEncryptionKeys.computeIfAbsent(cryptoKey.getTenantId(), tenantId -> new ArrayList<>()).add(cryptoKey);
		}
		if (allEncryptionKeys.isEmpty()) {
			logger.log(TRACE, "No keys found to rekey");
		}

		for (Map.Entry<String, List<CryptoKey>> tenantsCryptoKeysEntry : allEncryptionKeys.entrySet()) {
			try {
				rekeyTenant(tenantsCryptoKeysEntry.getKey(), tenantsCryptoKeysEntry.getValue());
			} catch (TooManyFailuresException e) {
				logger.log(ERROR, "Too many failures occurred trying to rekey records", e);
			}
		}
	}

	private void rekeyTenant(String tenantId, List<CryptoKey> tenantCryptoKeys) {
		if (tenantCryptoKeys.size() <= 1) {
			logger.log(ERROR, "There are no Crypto Keys defined{0}", tenantLogString(tenantId));
			return;
		}

		if (tenantCryptoKeys.stream().anyMatch(cryptoKey -> cryptoKey.getCreatedDate() == null)) {
			logger.log(ERROR, "Created date was not set to a valid value on some Crypto Keys{0}.....skipping rekey for this tenant", tenantLogString(tenantId));
			return;
		}

		List<CryptoKey> tenantAllCryptoKeysSortedByDateDescending = tenantCryptoKeys.stream()
				.sorted(Comparator.comparing(CryptoKey::getCreatedDate).reversed())
				.toList();

		// TODO: Currently we re-encrypt and re-HMAC separately which isn't very performant. Once this automated rekey code is
		//  settled and out of beta we need to come back and make that these are done together, especially for entities that have both
		//  encrypted data and HMACs
		try {
			reEncrypt(tenantId, tenantAllCryptoKeysSortedByDateDescending);
		} catch (Exception e) {
			logger.log(ERROR, "An error occurred trying to rekey encryption keys{0}", tenantLogString(tenantId));
		}

		try {
			reHmac(tenantId, tenantAllCryptoKeysSortedByDateDescending);
		} catch (Exception e) {
			logger.log(ERROR, "An error occurred trying to rekey HMAC keys{0}", tenantLogString(tenantId));
		}
	}

	private void reEncrypt(String tenantId, List<CryptoKey> tenantAllCryptoKeysSortedByDateDescending) {
		logger.log(TRACE, "Beginning re-encrypt for tenant {0}", tenantId);
		List<CryptoKey> tenantEncryptionKeysSortedByDateDescending = tenantAllCryptoKeysSortedByDateDescending.stream()
				.filter(cryptoKey -> cryptoKey.getUsage() == CryptoKeyUsage.ENCRYPTION)
				.toList();
		CryptoKey tenantLatestEncryptionKey = tenantEncryptionKeysSortedByDateDescending.stream()
				.findFirst()
				.orElse(null);
		if (tenantLatestEncryptionKey == null) {
			logger.log(INFO, "No encryption key was found{0}.....skipping the currently scheduled encryption rekey tasks for this tenant ", tenantLogString(tenantId));
			return;
		} else if (tenantLatestEncryptionKey.getCreatedDate().plus(cryptoKeyCacheDuration).isAfter(now())) {
			logger.log(DEBUG, "Some application instances might not be using the new encryption key yet{0}.....skipping the currently scheduled encryption rekey task for this tenant", tenantLogString(tenantId));
			return;
		}

		if (tenantLatestEncryptionKey.getRekeyMode() == KEY_ON) {
			logger.log(TRACE, "RekeyMode is set to {0} on the latest encryption key{1}", KEY_ON, tenantLogString(tenantId));
			if (doAnyEncryptedRecordsNeedRekeying(keyOnRecordSupplier(tenantLatestEncryptionKey))) {
				logger.log(TRACE, "Some records need re-encrypted{0}", tenantLogString(tenantId));
				RekeyCryptoShield rekeyCryptoShield = new RekeyCryptoShield(cryptoShield, tenantLatestEncryptionKey,
						getHmacKeysToRekeyTo(tenantAllCryptoKeysSortedByDateDescending));
				long totalEncryptedRecordsRekeyedForTenant = rekey(tenantLatestEncryptionKey, rekeyCryptoShield, keyOnRecordSupplier(tenantLatestEncryptionKey));
				logger.log(INFO, "Full re-key of all records has been completed{0}. Total of {1} records rekeyed to {2} by this job", tenantLogString(tenantId), totalEncryptedRecordsRekeyedForTenant, tenantLatestEncryptionKey);
			} else {
				logger.log(TRACE, "No records need re-encrypted{0}", tenantLogString(tenantId));
			}
			removeUnusedEncryptionKeys(tenantEncryptionKeysSortedByDateDescending);
		} else if (tenantEncryptionKeysSortedByDateDescending.stream().anyMatch(cryptoKey -> cryptoKey.getRekeyMode() == KEY_OFF)) {
			logger.log(TRACE, "RekeyMode is set to {0} on {2} encryption keys{1}", KEY_OFF, tenantLogString(tenantId), tenantEncryptionKeysSortedByDateDescending.stream().filter(cryptoKey -> cryptoKey.getRekeyMode() == KEY_OFF).count());
			if (tenantLatestEncryptionKey.getRekeyMode() == KEY_OFF) {
				logger.log(ERROR, "RekeyMode is set to {0} on the latest encryption key{1}. This is a misconfiguration, the latest encryption key should not be set to KEY_OFF!!.....skipping the currently scheduled encryption rekey task for this tenant", KEY_OFF, tenantLogString(tenantId));
				return;
			}
			for (CryptoKey encryptionKey : tenantEncryptionKeysSortedByDateDescending.subList(1, tenantEncryptionKeysSortedByDateDescending.size())) {
				if (encryptionKey.getRekeyMode() == KEY_OFF && encryptionKey.getCreatedDate().plus(cryptoKeyCacheDuration).isBefore(now())) {
					logger.log(TRACE, "Checking if there are any records using {0} to rekey", encryptionKey);
					if (doAnyEncryptedRecordsNeedRekeying(keyOffRecordSupplier(encryptionKey))) {
						logger.log(TRACE, "Some records need rekeyed{0}", tenantLogString(tenantId));
						RekeyCryptoShield rekeyCryptoShield = new RekeyCryptoShield(cryptoShield, tenantLatestEncryptionKey,
								getHmacKeysToRekeyTo(tenantAllCryptoKeysSortedByDateDescending));
						long totalEncryptedRecordsRekeyedForTenant = rekey(tenantLatestEncryptionKey, rekeyCryptoShield, keyOffRecordSupplier(encryptionKey));
						logger.log(INFO, "All records ({0}) using deprecated encryption key {1} have been keyed onto the current encryption key {2}",
								totalEncryptedRecordsRekeyedForTenant, encryptionKey, tenantLatestEncryptionKey);
					} else {
						logger.log(TRACE, "No records need keyed off {0}", encryptionKey);
					}
					logger.log(DEBUG, "No application instances are using the following deprecated encryption key anymore (and any records which previously used it have been re-keyed to the latest encryption key), so we''ll delete it: {0}", encryptionKey);
					removeKey(encryptionKey);
				}
			}
		} else {
			logger.log(TRACE, "No Re-keying needed{0}", tenantLogString(tenantId));
		}
	}

	private static String tenantLogString(String tenantId) {
		return tenantId == null ? "" : " for tenant " + tenantId;
	}

	/**
	 * There's only ever 1 active encryption key at one time, so we can delete any keys that aren't that key
	 *
	 * @param tenantEncryptionKeysSortedByDateDescending All of a tenants encryption keys in order of latest to oldest
	 */
	private void removeUnusedEncryptionKeys(List<CryptoKey> tenantEncryptionKeysSortedByDateDescending) {
		String tenantId = tenantEncryptionKeysSortedByDateDescending.get(0).getTenantId();
		logger.log(TRACE, "Attempting to remove any deprecated encryption keys{0}", tenantLogString(tenantId));
		if (tenantEncryptionKeysSortedByDateDescending.size() > 1) {
			// only remove the older keys, the first key in the list is the latest key (so don't touch that)
			tenantEncryptionKeysSortedByDateDescending.subList(1, tenantEncryptionKeysSortedByDateDescending.size()).clear();
			for (CryptoKey tenantEncryptionKey : tenantEncryptionKeysSortedByDateDescending.subList(1, tenantEncryptionKeysSortedByDateDescending.size())) {
				logger.log(DEBUG, "All application instances are now using only the latest encryption key{0} and are no longer using the following encryption key so we''ll mark it for deletion: {1}", tenantLogString(tenantId), tenantEncryptionKey);
				removeKey(tenantEncryptionKey);
			}
		} else {
			logger.log(DEBUG, "{0} only has a single encryption key, there''s no old encryption keys to delete", tenantLogString(tenantId));
		}
	}

	private void reHmac(String tenantId, List<CryptoKey> tenantAllCryptoKeysSortedByDateDescending) {
		List<CryptoKey> tenantHmacKeysSortedByDateDescending = getTenantHmacKeysSortedByDateDescending(tenantAllCryptoKeysSortedByDateDescending);
		if (tenantHmacKeysSortedByDateDescending.isEmpty()) {
			logger.log(INFO, "No HMACs to rekey{0}", tenantLogString(tenantId));
			return;
		} else if (tenantHmacKeysSortedByDateDescending.stream().anyMatch(cryptoKey -> cryptoKey.getCreatedDate().plus(cryptoKeyCacheDuration).isAfter(now()))) {
			logger.log(DEBUG, "Some application instances might not be using some of the HMAC keys yet{0}....." +
							"skipping the currently scheduled HMAC rekey tasks{0}",
					tenantLogString(tenantId));
			return;
		}

		if (doAnyRecordsWithHmacsNeedRekeying(tenantHmacKeysSortedByDateDescending)) {
			if (tenantHmacKeysSortedByDateDescending.get(0).getRekeyMode() == KEY_ON) {
				RekeyCryptoShield rekeyCryptoShield = new RekeyCryptoShield(cryptoShield, null,
						getHmacKeysToRekeyTo(tenantAllCryptoKeysSortedByDateDescending));
				long totalHmacRecordsRekeyedToTheCurrentKey = rekey(tenantAllCryptoKeysSortedByDateDescending.get(0), rekeyCryptoShield, keyOnRecordSupplier(tenantHmacKeysSortedByDateDescending.get(0)));
				logger.log(INFO, "Full HMAC re-key of all ({0}) records has been completed{1}", totalHmacRecordsRekeyedToTheCurrentKey, tenantLogString(tenantId));
				for (CryptoKey tenantHmacKey : tenantHmacKeysSortedByDateDescending) {
					if (canBeRemoved(tenantHmacKey)) {
						removeKey(tenantHmacKey);
					} else {
						logger.log(INFO, "The following HMAC key is not yet ready to be marked as deleted: {0}", tenantHmacKey);
					}
				}
			} else {
				tenantHmacKeysSortedByDateDescending.stream()
						.filter(hmacKey -> hmacKey.getRekeyMode() == KEY_OFF)
						.findFirst()
						.ifPresent(deprecatedHmacKey -> {
							RekeyCryptoShield rekeyCryptoShield = new RekeyCryptoShield(cryptoShield, null,
									getHmacKeysToRekeyTo(tenantAllCryptoKeysSortedByDateDescending));
							long totalHmacRecordsRekeyedForThisKey = rekey(deprecatedHmacKey, rekeyCryptoShield, keyOffRecordSupplier(deprecatedHmacKey));
							logger.log(INFO, "HMAC re-key of all ({0}) records using deprecated HMAC key {1} has been completed{2}",
									totalHmacRecordsRekeyedForThisKey, deprecatedHmacKey, tenantLogString(tenantId));
							removeKey(deprecatedHmacKey);
						});
			}
		}
	}

	private boolean canBeRemoved(CryptoKey tenantHmacKey) {
		return tenantHmacKey.getCreatedDate().plus(cryptoKeyCacheDuration).isBefore(now());
	}

	private boolean doAnyEncryptedRecordsNeedRekeying(Function<RekeyService<?>, List<?>> recordsNeedingRekeyedFunction) {
		List<?> recordsNeedingRekeyed;
		boolean doAnyRecordsNeedRekeying = false;
		for (RekeyService<?> rekeyService : rekeyServices) {
			try {
				recordsNeedingRekeyed = getRecords(rekeyService, recordsNeedingRekeyedFunction);
				if (recordsNeedingRekeyed != null && !recordsNeedingRekeyed.isEmpty()) {
					doAnyRecordsNeedRekeying = true;
					break;
				}
			} catch (Exception e) {
				logger.log(ERROR, "An error occurred trying to get records from RekeyService<{0}>", rekeyService.getEntityType());
			}
		}
		return doAnyRecordsNeedRekeying;
	}

	private boolean doAnyRecordsWithHmacsNeedRekeying(List<CryptoKey> tenantHmacKeys) {
		boolean doAnyRecordsNeedRekeying = false;
		for (CryptoKey tenantHmacKey : tenantHmacKeys) {
			if (doAnyRecordsNeedRekeying) {
				break;
			}

			Function<RekeyService<?>, List<?>> recordsNeedingRekeyedFunction = null;
			if (tenantHmacKey.getRekeyMode() == KEY_ON) {
				recordsNeedingRekeyedFunction = keyOnRecordSupplier(tenantHmacKey);
			} else if (tenantHmacKey.getRekeyMode() == KEY_OFF) {
				recordsNeedingRekeyedFunction = keyOffRecordSupplier(tenantHmacKey);
			}
			if (recordsNeedingRekeyedFunction != null) {
				for (RekeyService<?> rekeyService : rekeyServices) {
					List<?> recordsNeedingRekeyed = getRecords(rekeyService, recordsNeedingRekeyedFunction);
					if (recordsNeedingRekeyed != null && !recordsNeedingRekeyed.isEmpty()) {
						doAnyRecordsNeedRekeying = true;
						break;
					}
				}
			}
		}
		return doAnyRecordsNeedRekeying;
	}

	private List<?> getRecords(RekeyService<?> rekeyService, Function<RekeyService<?>, List<?>> recordsNeedingRekeyedFunction) {
		List<?> recordsNeedingRekeyed = null;
		try {
			recordsNeedingRekeyed = recordsNeedingRekeyedFunction.apply(rekeyService);
		} catch (Exception e) {
			logger.log(ERROR, "An error occurred trying to get records from RekeyService<{0}>", rekeyService.getEntityType());
		}
		return recordsNeedingRekeyed;
	}

	private List<CryptoKey> getHmacKeysToRekeyTo(List<CryptoKey> tenantAllCryptoKeysSortedByDateDescending) {
		List<CryptoKey> tenantHmacKeysSortedByDateDescending = getTenantHmacKeysSortedByDateDescending(tenantAllCryptoKeysSortedByDateDescending);
		if (tenantHmacKeysSortedByDateDescending.isEmpty()) {
			logger.log(DEBUG, "No HMAC keys exist{0}", tenantLogString(tenantAllCryptoKeysSortedByDateDescending.get(0).getTenantId()));
			return emptyList();
		} else if (tenantHmacKeysSortedByDateDescending.stream().anyMatch(cryptoKey -> cryptoKey.getCreatedDate().plus(cryptoKeyCacheDuration).isAfter(now()))) {
			logger.log(DEBUG, "Some application instances might not be using some of the HMAC keys yet{0}....." +
							"so we''ll skip rekeying any HMACs{0}",
					tenantLogString(tenantHmacKeysSortedByDateDescending.get(0).getTenantId()));
			return emptyList();
		}

		List<CryptoKey> tenantHmacKeysToRekeyTo = new ArrayList<>();
		if (tenantHmacKeysSortedByDateDescending.get(0).getRekeyMode() == KEY_ON) {
			// KEY_ON means rekey all HMACs to this key
			tenantHmacKeysToRekeyTo = List.of(tenantHmacKeysSortedByDateDescending.get(0));
		} else if (tenantHmacKeysSortedByDateDescending.stream()
				.anyMatch(tenantHmacKey -> tenantHmacKey.getRekeyMode() == KEY_OFF)) {
			// KEY_OFF means rekey to all HMAC keys that are not KEY_OFF
			tenantHmacKeysToRekeyTo = tenantHmacKeysSortedByDateDescending.stream()
					.filter(tenantHmacKey -> tenantHmacKey.getRekeyMode() != KEY_OFF)
					.toList();
		}
		return tenantHmacKeysToRekeyTo;
	}

	private static List<CryptoKey> getTenantHmacKeysSortedByDateDescending(List<CryptoKey> tenantAllCryptoKeysSortedByDateDescending) {
		return tenantAllCryptoKeysSortedByDateDescending.stream()
				.filter(cryptoKey -> cryptoKey.getUsage() == CryptoKeyUsage.HMAC)
				.toList();
	}

	private static Function<RekeyService<?>, List<?>> keyOnRecordSupplier(CryptoKey cryptoKey) {
		return (rekeyService) -> rekeyService.findRecordsNotUsingCryptoKey(cryptoKey);
	}

	private static Function<RekeyService<?>, List<?>> keyOffRecordSupplier(CryptoKey cryptoKey) {
		return (rekeyService) -> rekeyService.findRecordsUsingCryptoKey(cryptoKey);
	}

	@SuppressWarnings("BusyWait")
	private long rekey(CryptoKey cryptoKey, RekeyCryptoShield rekeyCryptoShield, Function<RekeyService<?>, List<?>> recordsSupplier) {
		logger.log(DEBUG, "Running re-key job. Keying {0} CryptoKey: {1}",
				cryptoKey.getRekeyMode() == KEY_OFF ? "off" : "on", cryptoKey);
		AtomicLong count = new AtomicLong();
		rekeyServices.forEach((rekeyService) -> {
			logger.log(DEBUG, "Checking for records to re-key for entity {0}", rekeyService.getEntityType().getName());
			ProgressTracker entityRekeyProgressTracker = new ProgressTracker(maximumToleratedFailuresPerExecution);
			while (!rekeyBatch(rekeyService.getEntityType(), rekeyService, entityRekeyProgressTracker, rekeyCryptoShield, recordsSupplier)) {
				if (!Duration.ZERO.equals(batchInterval)) {
					try {
						logger.log(DEBUG, "Waiting for {0} milliseconds before re-keying another batch of records for entity {1}", batchInterval, rekeyService.getEntityType().getName());
						Thread.sleep(batchInterval.toMillis());
					} catch (InterruptedException e) {
						logger.log(ERROR, String.format("An error occurred waiting to re-key the next batch of records for entity %s....cancelling this re-key task", rekeyService.getEntityType().getName()), e);
						Thread.currentThread().interrupt();
						return;
					}
				}
			}

			if (entityRekeyProgressTracker.getNumberOfRecordsProcessed() > 0) {
				logger.log(INFO, "Re-key complete for {0}.", rekeyService.getEntityType().getName());
				RekeyEvent rekeyFinishedEvent = new RekeyEvent();
				rekeyFinishedEvent.setRekeyServiceClass(rekeyService.getClass());
				rekeyFinishedEvent.setCryptoKey(cryptoKey);
				rekeyFinishedEvent.setType(REKEY_FINISHED);
				rekeyFinishedEvent.setProgressTracker(entityRekeyProgressTracker);
				rekeyService.notify(rekeyFinishedEvent);
			}
			count.addAndGet(entityRekeyProgressTracker.getNumberOfRecordsProcessed());
		});
		return count.get();
	}

	/**
	 * @return true if there were no records in this batch to process, meaning that we're finished re-keying this entity. False otherwise.
	 */
	private boolean rekeyBatch(Class<?> entityClass, RekeyService<?> rekeyService, ProgressTracker progressTracker,
							   RekeyCryptoShield rekeyCryptoShield, Function<RekeyService<?>, List<?>> recordsSupplier) {
		progressTracker.incrementBatchesProcessed();
		List<?> recordsToRekey = recordsSupplier.apply(rekeyService);
		if (recordsToRekey == null || recordsToRekey.isEmpty()) {
			logger.log(DEBUG, "No more records to re-key for entity {0}", entityClass.getName());
			return true;
		}

		logger.log(DEBUG, "Found {0} records to re-key for entity {1}", recordsToRekey.size(), entityClass.getName());
		for (Object entity : recordsToRekey) {
			rekeyEntity(entity, progressTracker, rekeyCryptoShield);
		}
		logger.log(DEBUG, "{0} records in batch {1} re-keyed for entity {2}....saving",
				progressTracker.getNumberOfRecordsProcessed() - progressTracker.getNumberOfRecordsFailed(),
				progressTracker.getNumberOfBatchesProcessed(), entityClass.getName());
		try {
			rekeyService.save(recordsToRekey);
		} catch (Exception e) {
			progressTracker.incrementNumberOfRecordsFailed();
			logger.log(WARNING, "An error occurred trying to save the {0} batch of records for entity {1}....skipping record",
					convertToOrdinal(progressTracker.getNumberOfBatchesProcessed()), entityClass.getName());
		}
		logger.log(DEBUG, () -> String.format("%s re-keyed records successfully saved for %s batch for entity %s (%s failed)",
				progressTracker.getNumberOfRecordsProcessed() - progressTracker.getNumberOfRecordsFailed(), convertToOrdinal(progressTracker.getNumberOfBatchesProcessed()),
				entityClass.getName(), progressTracker.getNumberOfRecordsFailed()));
		return false;
	}

	private void rekeyEntity(Object entity, ProgressTracker progressTracker, RekeyCryptoShield rekeyCryptoShield) {
		progressTracker.incrementRecordsProcessed();
		try {
			logger.log(TRACE, () -> String.format("Re-keying %s record in batch %s",
					convertToOrdinal(progressTracker.getNumberOfRecordsProcessed()), progressTracker.getNumberOfBatchesProcessed()));
			try {
				rekeyCryptoShield.decrypt(entity);
			} catch (Exception e) {
				logger.log(ERROR, "An error occurred trying to decrypt entity");
				throw new RuntimeException(e);
			}
			try {
				rekeyCryptoShield.encrypt(entity);
			} catch (Exception e) {
				logger.log(ERROR, "An error occurred trying to re-encrypt entity");
				throw new RuntimeException(e);
			}
			logger.log(TRACE, () -> String.format("%s record in batch %s re-keyed successfully",
					convertToOrdinal(progressTracker.getNumberOfRecordsProcessed()), progressTracker.getNumberOfBatchesProcessed()));
		} catch (Exception e) {
			progressTracker.incrementNumberOfRecordsFailed();
			logger.log(WARNING, "An error occurred trying to re-key the {0} record in the {1} batch of records for entity {2}....skipping record",
					convertToOrdinal(progressTracker.getNumberOfRecordsProcessed()), convertToOrdinal(progressTracker.getNumberOfBatchesProcessed()), entity.getClass().getName());
		}
	}

	private void removeKey(CryptoKey tenantsDeprecatedCryptoKey) {
		try {
			logger.log(INFO, "Notifying the application to mark the following Crypto key as deleted {1}", tenantsDeprecatedCryptoKey);
			rekeyCryptoKeyManager.markKeyForDeletion(tenantsDeprecatedCryptoKey);
		} catch (Exception e) {
			logger.log(ERROR, "An error occurred trying to mark the Crypto key for deletion {}", tenantsDeprecatedCryptoKey);
		}
	}

	private static String convertToOrdinal(int i) {
		return switch (i % 100) {
			case 11, 12, 13 -> i + "th";
			default -> i + ORDINAL_SUFFIXES[i % 10];
		};
	}

	private Instant now() {
		return clock.instant();
	}
}