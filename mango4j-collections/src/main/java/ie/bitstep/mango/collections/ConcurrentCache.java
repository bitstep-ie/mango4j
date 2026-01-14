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

	public ConcurrentCache(long cacheTimeToLive, TimeUnit unit, ScheduledExecutorService cleaner, Clock clock) {
		this(Duration.ofMillis(unit.toMillis(cacheTimeToLive)), Duration.ofMillis(unit.toMillis(cacheTimeToLive)), DEFAULT_GRACE_PERIOD, DEFAULT_EVICTION_TASK_PERIOD, cleaner, clock);
	}

	public void setCacheEntryTTL(Duration cacheEntryTTL) {
		this.cacheEntryTTL = cacheEntryTTL;
		logger.log(TRACE, "cacheEntryTTL set to {0}", cacheEntryTTL);
	}

	public void setCurrentEntryTTL(Duration currentEntryTTL) {
		this.currentEntryTTL = currentEntryTTL;
		logger.log(TRACE, "currentEntryTTL set to {0}", currentEntryTTL);
	}

	public Duration getCacheEntryTTL() {
		return cacheEntryTTL;
	}

	public Duration getCurrentEntryTTL() {
		return currentEntryTTL;
	}

	public void setCacheGracePeriod(Duration cacheGracePeriod) {
		this.cacheGracePeriod = cacheGracePeriod;
	}

	public Duration getCacheGracePeriod() {
		return cacheGracePeriod;
	}

	public V put(K key, V value) {
		logger.log(TRACE, "Putting new entry into cache");
		cache.put(key, new CacheEntry<>(value, cacheEntryTTL));
		logger.log(TRACE, "New entry put into cache");
		return value;
	}

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

	public V putCurrent(K key, V value) {
		logger.log(TRACE, "Adding new current entry");
		evictCurrentKey();
		logger.log(TRACE, "New current entry added to cache");
		currentEntry.set(new Map.Entry<>() {
			private final CacheEntry<V> currentCacheEntry = new CacheEntry<>(value, currentEntryTTL);

			@Override
			public K getKey() {
				return key;
			}

			@Override
			public CacheEntry<V> getValue() {
				return currentCacheEntry;
			}

			@Override
			public CacheEntry<V> setValue(CacheEntry<V> value) {
				throw new UnsupportedOperationException();
			}
		});
		logger.log(TRACE, "New current entry set");
		return value;
	}

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

	private boolean shouldExpire(CacheEntry<V> entry) {
		return entry.expiryDate.isBefore(now());
	}

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

	private void moveToEvictedEntries(Map.Entry<K, CacheEntry<V>> cacheEntry) {
		if (cacheEntry != null && cacheEntry.getValue().value instanceof AutoCloseable) {
			cacheEntry.getValue().setExpiryDate(clock.instant().plus(cacheGracePeriod));
			evictedEntries.add(cacheEntry.getValue());
		}
	}

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

	private Instant now() {
		return clock.instant();
	}

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

		CacheEntry(T value, Duration ttl) {
			this.value = value;
			this.expiryDate = clock.instant().plus(ttl);
		}

		void setExpiryDate(Instant newExpiryDate) {
			expiryDate = newExpiryDate;
			logger.log(TRACE, "Updated expiryDate to {0}", expiryDate);
		}
	}
}