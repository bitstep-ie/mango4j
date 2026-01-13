package ie.bitstep.mango.crypto.keyrotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.bitstep.mango.crypto.CryptoShield;
import ie.bitstep.mango.crypto.RekeyCryptoShield;
import ie.bitstep.mango.crypto.core.domain.CryptoKey;
import ie.bitstep.mango.crypto.core.domain.CryptoKeyUsage;
import ie.bitstep.mango.crypto.core.providers.CryptoKeyProvider;
import ie.bitstep.mango.crypto.annotations.EncryptedBlob;
import ie.bitstep.mango.crypto.core.encryption.EncryptionService;
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

	private void rekeyTenants() {
		Map<String, List<CryptoKey>> allEncryptionKeys = new HashMap<>();
		for (CryptoKey cryptoKey : cryptoShield.getCryptoKeyProvider().getAllCryptoKeys()) {
			allEncryptionKeys.computeIfAbsent(cryptoKey.getTenantId(), tenantId -> new ArrayList<>()).add(cryptoKey);
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
			logger.log(ERROR, "Created date was not set to a valid value on some Crypto Keys{0}", tenantLogString(tenantId));
			return;
		}

		List<CryptoKey> tenantAllCryptoKeysSortedByDateDescending = tenantCryptoKeys.stream()
			.sorted(Comparator.comparing(CryptoKey::getCreatedDate).reversed())
			.toList();

		try {
			reEncrypt(tenantId, tenantAllCryptoKeysSortedByDateDescending);
		} catch (Exception e) {
			logger.log(ERROR, "An error occurred trying to rekey encryption keys{0}", tenantLogString(tenantId));
		}

		try {
			reHmac(tenantAllCryptoKeysSortedByDateDescending);
		} catch (Exception e) {
			logger.log(ERROR, "An error occurred trying to rekey HMAC keys{0}", tenantLogString(tenantId));
		}
	}

	private void reEncrypt(String tenantId, List<CryptoKey> tenantAllCryptoKeysSortedByDateDescending) {
		List<CryptoKey> tenantEncryptionKeysSortedByDateDescending = tenantAllCryptoKeysSortedByDateDescending.stream()
			.filter(cryptoKey -> cryptoKey.getUsage() == CryptoKeyUsage.ENCRYPTION)
			.toList();
		CryptoKey tenantCurrentEncryptionKey = tenantEncryptionKeysSortedByDateDescending.stream()
			.findFirst()
			.orElse(null);
		if (tenantCurrentEncryptionKey == null) {
			logger.log(INFO, "No current encryption key was found{0}", tenantLogString(tenantId));
			return;
		} else if (tenantCurrentEncryptionKey.getCreatedDate().plus(cryptoKeyCacheDuration).isAfter(now())) {
			logger.log(DEBUG, "Some application instances might not be using the new encryption key yet{0}.....skipping the currently scheduled encryption rekey tasks for this tenant", tenantLogString(tenantId));
			return;
		}

		if (tenantCurrentEncryptionKey.getRekeyMode() == KEY_ON) {
			if (doAnyEncryptedRecordsNeedRekeying(keyOnRecordSupplier(tenantCurrentEncryptionKey))) {
				RekeyCryptoShield rekeyCryptoShield = new RekeyCryptoShield(cryptoShield, tenantCurrentEncryptionKey,
					getHmacKeysToRekeyTo(tenantAllCryptoKeysSortedByDateDescending));
				rekey(tenantCurrentEncryptionKey, rekeyCryptoShield, keyOnRecordSupplier(tenantCurrentEncryptionKey));
				logger.log(INFO, "Full re-key of all records has been completed{0}", tenantLogString(tenantId));
				removeUnusedEncryptionKeys(tenantEncryptionKeysSortedByDateDescending, tenantCurrentEncryptionKey);
			} else {
				removeUnusedEncryptionKeys(tenantEncryptionKeysSortedByDateDescending, tenantCurrentEncryptionKey);
			}
		} else if (tenantEncryptionKeysSortedByDateDescending.stream().anyMatch(cryptoKey -> cryptoKey.getRekeyMode() == KEY_OFF)) {
			for (CryptoKey encryptionKey : tenantEncryptionKeysSortedByDateDescending) {
				if (encryptionKey.getRekeyMode() == KEY_OFF && !encryptionKey.equals(tenantCurrentEncryptionKey)
					&& encryptionKey.getCreatedDate().plus(cryptoKeyCacheDuration).isBefore(now())) {
					if (doAnyEncryptedRecordsNeedRekeying(keyOffRecordSupplier(encryptionKey))) {
						RekeyCryptoShield rekeyCryptoShield = new RekeyCryptoShield(cryptoShield, tenantCurrentEncryptionKey,
							getHmacKeysToRekeyTo(tenantAllCryptoKeysSortedByDateDescending));
						rekey(tenantCurrentEncryptionKey, rekeyCryptoShield, keyOffRecordSupplier(encryptionKey));
						logger.log(INFO, "All records using deprecated encryption key {0} have been keyed onto the current encryption key {1}", encryptionKey, tenantCurrentEncryptionKey);
					} else if (!encryptionKey.equals(tenantCurrentEncryptionKey)) {
						logger.log(DEBUG, "No application instances are using the following deprecated encryption key anymore (and any records which previously used it have been re-keyed to the latest encryption key), so we''ll delete it: {0}", encryptionKey);
						removeKey(encryptionKey);
					}
				}
			}
		} else {
			logger.log(TRACE, "No Re-keying needed{0}", tenantLogString(tenantId));
		}
	}

	private static String tenantLogString(String tenantId) {
		return tenantId == null ? "" : " for tenant " + tenantId;
	}

	private void removeUnusedEncryptionKeys(List<CryptoKey> tenantEncryptionKeysSortedByDateDescending, CryptoKey tenantCurrentEncryptionKey) {
		if (tenantEncryptionKeysSortedByDateDescending.size() > 1) {
			for (CryptoKey tenantEncryptionKey : tenantEncryptionKeysSortedByDateDescending) {
				if (!tenantEncryptionKey.equals(tenantCurrentEncryptionKey)) {
					logger.log(DEBUG, "All application instances are now using only the current encryption key and are no longer using the following encryption key so we''ll delete it: {0}", tenantEncryptionKey);
					removeKey(tenantEncryptionKey);
				}
			}
		}
	}

	private void reHmac(List<CryptoKey> tenantAllCryptoKeysSortedByDateDescending) {
		List<CryptoKey> tenantHmacKeysSortedByDateDescending = getTenantHmacKeysSortedByDateDescending(tenantAllCryptoKeysSortedByDateDescending);
		if (tenantHmacKeysSortedByDateDescending.isEmpty()) {
			logger.log(INFO, "No HMACs to rekey{0}'", tenantLogString(tenantAllCryptoKeysSortedByDateDescending.get(0).getTenantId()));
			return;
		} else if (tenantHmacKeysSortedByDateDescending.stream().anyMatch(cryptoKey -> cryptoKey.getCreatedDate().plus(cryptoKeyCacheDuration).isAfter(now()))) {
			logger.log(DEBUG, "Some application instances might not be using some of the HMAC keys yet{0}....." +
					"skipping the currently scheduled HMAC rekey tasks{0}",
				tenantLogString(tenantHmacKeysSortedByDateDescending.get(0).getTenantId()));
			return;
		}

		if (doAnyRecordsWithHmacsNeedRekeying(tenantHmacKeysSortedByDateDescending)) {
			if (tenantHmacKeysSortedByDateDescending.get(0).getRekeyMode() == KEY_ON) {
				RekeyCryptoShield rekeyCryptoShield = new RekeyCryptoShield(cryptoShield, null,
					getHmacKeysToRekeyTo(tenantAllCryptoKeysSortedByDateDescending));
				rekey(tenantAllCryptoKeysSortedByDateDescending.get(0), rekeyCryptoShield, keyOnRecordSupplier(tenantHmacKeysSortedByDateDescending.get(0)));
				for (int i = 1; i < tenantHmacKeysSortedByDateDescending.size(); i++) {
					CryptoKey cryptoKey = tenantHmacKeysSortedByDateDescending.get(i);
					if (canBeRemoved(tenantHmacKeysSortedByDateDescending, i)) {
						removeKey(cryptoKey);
					}
				}
			} else {
				tenantHmacKeysSortedByDateDescending.stream().filter(hmacKey -> hmacKey.getRekeyMode() == KEY_OFF)
					.findFirst()
					.ifPresent(deprecatedHmacKey -> {
						RekeyCryptoShield rekeyCryptoShield = new RekeyCryptoShield(cryptoShield, null,
							getHmacKeysToRekeyTo(tenantAllCryptoKeysSortedByDateDescending));
						rekey(deprecatedHmacKey, rekeyCryptoShield, keyOffRecordSupplier(deprecatedHmacKey));
						removeKey(deprecatedHmacKey);
					});
			}
		}
	}

	private boolean canBeRemoved(List<CryptoKey> tenantHmacKeysSortedByDateDescending, int i) {
		return tenantHmacKeysSortedByDateDescending.get(i).getCreatedDate().plus(cryptoKeyCacheDuration).isBefore(now());
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
			logger.log(DEBUG, "No HMAC keys exist{0}'", tenantLogString(tenantAllCryptoKeysSortedByDateDescending.get(0).getTenantId()));
			return emptyList();
		} else if (tenantHmacKeysSortedByDateDescending.stream().anyMatch(cryptoKey -> cryptoKey.getCreatedDate().plus(cryptoKeyCacheDuration).isAfter(now()))) {
			logger.log(DEBUG, "Some application instances might not be using some of the HMAC keys yet{0}....." +
					"so we'll skip rekeying any HMACs{0}",
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
	private void rekey(CryptoKey cryptoKey, RekeyCryptoShield rekeyCryptoShield, Function<RekeyService<?>, List<?>> recordsSupplier) {
		logger.log(DEBUG, "Running re-key job. Keying {0} CryptoKey: {1}",
			cryptoKey.getRekeyMode() == KEY_OFF ? "off" : "on", cryptoKey);
		AtomicLong count = new AtomicLong();
		rekeyServices.forEach((rekeyService) -> {
			logger.log(DEBUG, "Checking for records to re-key for entity {0}", rekeyService.getEntityType().getName());
			ProgressTracker progressTracker = new ProgressTracker(maximumToleratedFailuresPerExecution);
			while (!rekeyBatch(rekeyService.getEntityType(), rekeyService, progressTracker, rekeyCryptoShield, recordsSupplier)) {
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

			if (progressTracker.getNumberOfRecordsProcessed() > 0) {
				logger.log(INFO, "Re-key complete for {0}.", rekeyService.getEntityType().getName());
				rekeyService.notify(progressTracker);
			}
			count.addAndGet(progressTracker.getNumberOfRecordsProcessed());
		});
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
				rekeyCryptoShield.protect(entity);
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
			rekeyCryptoKeyManager.deleteKey(tenantsDeprecatedCryptoKey);
		} catch (Exception e) {
			logger.log(ERROR, "An error occurred trying to delete Crypto Key {}", tenantsDeprecatedCryptoKey);
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
}