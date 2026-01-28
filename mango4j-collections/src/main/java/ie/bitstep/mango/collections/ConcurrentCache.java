package ie.bitstep.mango.collections;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;

public class ConcurrentCache<K, V> {
	private final System.Logger logger = System.getLogger(ConcurrentCache.class.getName());

	public static final Duration DEFAULT_EVICTION_TASK_PERIOD = Duration.ofMinutes(1);
	public static final Duration DEFAULT_GRACE_PERIOD = Duration.ofSeconds(5);

	private Duration cacheEntryTTL;
	private Duration cacheGracePeriod;
	private Duration currentEntryTTL;
	private final ConcurrentHashMap<K, CacheEntry<V>> cache;
	private final Set<CacheEntry<V>> evictedEntries = ConcurrentHashMap.newKeySet();
	private final ScheduledExecutorService cleaner;
	private final AtomicReference<Map.Entry<K, CacheEntry<V>>> currentEntry = new AtomicReference<>();
	private final Clock clock;

	/**
	 * Creates a cache with explicit TTLs and an eviction scheduler.
	 *
	 * @param cacheEntryTTL time-to-live for standard entries
	 * @param currentEntryTTL time-to-live for the current entry
	 * @param gracePeriod grace period before closing evicted entries
	 * @param evictionTaskPeriod scheduler period for eviction tasks
	 * @param cleaner scheduler used to run eviction tasks
	 * @param clock clock used to compute expiry times
	 */
	public ConcurrentCache(Duration cacheEntryTTL, Duration currentEntryTTL, Duration gracePeriod, Duration evictionTaskPeriod, ScheduledExecutorService cleaner, Clock clock) {
		this.cacheGracePeriod = gracePeriod;
		this.cleaner = cleaner;
		this.clock = clock;
		this.cacheEntryTTL = cacheEntryTTL;
		this.currentEntryTTL = currentEntryTTL;
		cache = new ConcurrentHashMap<>();
		cleaner.scheduleAtFixedRate(this::expiredCacheEntriesEvictionTask, evictionTaskPeriod.toMillis(), evictionTaskPeriod.toMillis(), TimeUnit.MILLISECONDS);
		cleaner.scheduleAtFixedRate(this::purgeTask, evictionTaskPeriod.toMillis(), evictionTaskPeriod.toMillis(), TimeUnit.MILLISECONDS);
		logger.log(TRACE, "ConcurrentCache instance created with cacheEntryTTL duration of {0}, currentEntryTTL of {1}", cacheEntryTTL, currentEntryTTL);
	}

	/**
	 * Creates a cache with a single TTL and default eviction settings.
	 *
	 * @param cacheTimeToLive cache time-to-live
	 * @param unit the unit for the TTL
	 * @param cleaner scheduler used to run eviction tasks
	 * @param clock clock used to compute expiry times
	 */
	public ConcurrentCache(long cacheTimeToLive, TimeUnit unit, ScheduledExecutorService cleaner, Clock clock) {
		this(Duration.ofMillis(unit.toMillis(cacheTimeToLive)), Duration.ofMillis(unit.toMillis(cacheTimeToLive)), DEFAULT_GRACE_PERIOD, DEFAULT_EVICTION_TASK_PERIOD, cleaner, clock);
	}

	/**
	 * Updates the TTL for cache entries.
	 *
	 * @param cacheEntryTTL new TTL for entries
	 */
	public void setCacheEntryTTL(Duration cacheEntryTTL) {
		this.cacheEntryTTL = cacheEntryTTL;
		logger.log(TRACE, "cacheEntryTTL set to {0}", cacheEntryTTL);
	}

	/**
	 * Updates the TTL for the current entry.
	 *
	 * @param currentEntryTTL new TTL for current entry
	 */
	public void setCurrentEntryTTL(Duration currentEntryTTL) {
		this.currentEntryTTL = currentEntryTTL;
		logger.log(TRACE, "currentEntryTTL set to {0}", currentEntryTTL);
	}

	/**
	 * Returns the configured TTL for cache entries.
	 *
	 * @return the entry TTL
	 */
	public Duration getCacheEntryTTL() {
		return cacheEntryTTL;
	}

	/**
	 * Returns the configured TTL for the current entry.
	 *
	 * @return the current entry TTL
	 */
	public Duration getCurrentEntryTTL() {
		return currentEntryTTL;
	}

	/**
	 * Updates the grace period for evicted entries.
	 *
	 * @param cacheGracePeriod new grace period
	 */
	public void setCacheGracePeriod(Duration cacheGracePeriod) {
		this.cacheGracePeriod = cacheGracePeriod;
	}

	/**
	 * Returns the grace period for evicted entries.
	 *
	 * @return the grace period
	 */
	public Duration getCacheGracePeriod() {
		return cacheGracePeriod;
	}

	/**
	 * Adds or replaces an entry in the cache.
	 *
	 * @param key the cache key
	 * @param value the cache value
	 * @return the stored value
	 */
	public V put(K key, V value) {
		logger.log(TRACE, "Putting new entry into cache");
		cache.put(key, new CacheEntry<>(value, cacheEntryTTL));
		logger.log(TRACE, "New entry put into cache");
		return value;
	}

	/**
	 * Retrieves an entry by key, extending its TTL on access.
	 *
	 * @param key the cache key
	 * @return the cached value, or null if missing
	 */
	public V get(K key) {
		V result = null;
		logger.log(TRACE, "Getting key from cache");
		CacheEntry<V> entry = cache.get(key);
		if (entry != null) {
			logger.log(TRACE, "Key found in cache");
			entry.setExpiryDate(clock.instant().plus(cacheEntryTTL));
			result = entry.value;
		} else {
			Map.Entry<K, CacheEntry<V>> current = this.currentEntry.get();
			if (current != null && key.equals(current.getKey())) {
				logger.log(TRACE, "Key is the current entry");
				result = current.getValue().value;
			} else {
				logger.log(TRACE, "Key doesn't exist in cache and isn't the current key either");
			}
		}
		return result;
	}

	/**
	 * Stores a value as the current entry, evicting any previous current entry.
	 *
	 * @param key the cache key
	 * @param value the cache value
	 * @return the stored value
	 */
	public V putCurrent(K key, V value) {
		logger.log(TRACE, "Adding new current entry");
		evictCurrentKey();
		logger.log(TRACE, "New current entry added to cache");
			currentEntry.set(new Map.Entry<>() {
				private final CacheEntry<V> currentCacheEntry = new CacheEntry<>(value, currentEntryTTL);

				/**
				 * Returns the current entry key.
				 *
				 * @return the cache key
				 */
				@Override
				public K getKey() {
					return key;
				}

				/**
				 * Returns the current entry value wrapper.
				 *
				 * @return the cache entry value
				 */
				@Override
				public CacheEntry<V> getValue() {
					return currentCacheEntry;
				}

				/**
				 * Unsupported operation for the current entry.
				 *
				 * @param value ignored
				 * @return never returns normally
				 */
				@Override
				public CacheEntry<V> setValue(CacheEntry<V> value) {
					throw new UnsupportedOperationException();
				}
			});
		logger.log(TRACE, "New current entry set");
		return value;
	}

	/**
	 * Returns the current entry value, if present.
	 *
	 * @return the current value or null
	 */
	public V getCurrent() {
		logger.log(TRACE, "Getting current entry from cache");
		// the scheduled threads mess with this.currentEntry so assign to a new local variable and use that to avoid a race condition
		Map.Entry<K, CacheEntry<V>> entry = this.currentEntry.get();
		if (entry == null) {
			logger.log(TRACE, "current entry entry is null");
			return null;
		}

		V value = entry.getValue().value;
		if (value == null) {
			logger.log(INFO, "current entry value is null");
		}
		return value;
	}

	/**
	 * Checks whether a cache entry has expired.
	 *
	 * @param entry the entry to check
	 * @return true if expired
	 */
	private boolean shouldExpire(CacheEntry<V> entry) {
		return entry.expiryDate.isBefore(now());
	}

	/**
	 * Eviction task that removes expired entries and handles the current entry.
	 */
	private void expiredCacheEntriesEvictionTask() {
		for (Map.Entry<K, CacheEntry<V>> cacheEntry : cache.entrySet()) {
			CacheEntry<V> entry = cacheEntry.getValue();
			// Use atomic remove to prevent race condition where value is updated between check and remove
			if (shouldExpire(entry) && cache.remove(cacheEntry.getKey(), entry)) {
				moveToEvictedEntries(cacheEntry);
			}
		}
		Map.Entry<K, CacheEntry<V>> entry = this.currentEntry.get();
		if (entry != null && shouldExpire(entry.getValue())) {
			evictCurrentKey();
		}
	}

	/**
	 * Moves an entry to the evicted set when it implements {@link AutoCloseable}.
	 *
	 * @param cacheEntry the entry to evict
	 */
	private void moveToEvictedEntries(Map.Entry<K, CacheEntry<V>> cacheEntry) {
		if (cacheEntry != null && cacheEntry.getValue().value instanceof AutoCloseable) {
			cacheEntry.getValue().setExpiryDate(clock.instant().plus(cacheGracePeriod));
			evictedEntries.add(cacheEntry.getValue());
		}
	}

	/**
	 * Evicts the current entry, if present.
	 */
	private void evictCurrentKey() {
		logger.log(TRACE, "Evicting current entry");
		Map.Entry<K, CacheEntry<V>> currentEntryToEvict;
		if ((currentEntryToEvict = currentEntry.getAndSet(null)) == null) {
			// 2 threads could have come in here at the same time - just need to handle that race condition
			return;
		}
		logger.log(TRACE, "current entry evicted");
		moveToEvictedEntries(currentEntryToEvict);
	}

	/**
	 * Cleanup task that closes expired evicted entries.
	 */
	private void purgeTask() {
		evictedEntries.removeIf(expiredObject -> {
			if (expiredObject.expiryDate.isBefore(now())) {
				try {
					logger.log(TRACE, "Evicted entry is an autocloseable resource....calling close()");
					((AutoCloseable) expiredObject.value).close();
					logger.log(TRACE, "Evicted entry resource closed");
				} catch (Exception e) {
					logger.log(ERROR, "An error occurred attempting to call .close() on evicted entry resource", e);
					// swallow exception
				}
				return true; // Remove from set after closing
			}
			return false; // Keep in set if not yet expired
		});
	}

	/**
	 * Returns the current instant from the configured clock.
	 *
	 * @return the current instant
	 */
	private Instant now() {
		return clock.instant();
	}

	/**
	 * Shuts down scheduled eviction and clears all entries.
	 */
	public void shutdown() {
		logger.log(TRACE, "Shutdown called....shutting down cleaner task");
		cleaner.shutdown();
		logger.log(TRACE, "Cleaner task shutdown");

		// Clean up all remaining entries
		clear();

		// Force immediate cleanup of all evicted entries
		purgeAllEvictedEntries();

		logger.log(TRACE, "All resources cleaned up");
	}

	/**
	 * Closes and removes all evicted entries immediately.
	 */
	private void purgeAllEvictedEntries() {
		evictedEntries.removeIf(entry -> {
			try {
				if (entry.value instanceof AutoCloseable autoCloseable) {
					logger.log(TRACE, "Closing resource during shutdown");
					autoCloseable.close();
					logger.log(TRACE, "Resource closed during shutdown");
				}
			} catch (Exception e) {
				logger.log(ERROR, "Error closing resource during shutdown", e);
			}
			return true; // Remove all entries
		});
	}

	/**
	 * Clears all entries and schedules close of evicted resources.
	 */
	public void clear() {
		logger.log(TRACE, "Clearing cache");
		Set<Map.Entry<K, CacheEntry<V>>> cacheEntries = new HashMap<>(cache).entrySet();
		cache.clear();
		Map.Entry<K, CacheEntry<V>> currentEntryToEvict = currentEntry.getAndSet(null);
		// to narrow down race conditions where the cache returns resources which have already been cleaned up we clean
		// up all resources as the last step by moving them to the evictedEntries set and letting the purgeTask clean them up
		moveToEvictedEntries(currentEntryToEvict);
		for (Map.Entry<K, CacheEntry<V>> cacheEntry : cacheEntries) {
			moveToEvictedEntries(cacheEntry);
		}
	}

	// hate to do it but needs default access for test
	class CacheEntry<T> {
		final T value;
		private volatile Instant expiryDate;

		/**
		 * Creates a cache entry with the specified TTL.
		 *
		 * @param value the cached value
		 * @param ttl the time-to-live
		 */
		CacheEntry(T value, Duration ttl) {
			this.value = value;
			this.expiryDate = clock.instant().plus(ttl);
		}

		/**
		 * Updates the expiry time for the entry.
		 *
		 * @param newExpiryDate the new expiry time
		 */
		void setExpiryDate(Instant newExpiryDate) {
			expiryDate = newExpiryDate;
			logger.log(TRACE, "Updated expiryDate to {0}", expiryDate);
		}
	}
}
