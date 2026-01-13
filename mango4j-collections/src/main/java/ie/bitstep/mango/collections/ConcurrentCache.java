package ie.bitstep.mango.collections;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentCache<K, V> {
	private Duration cacheEntryTTL;
	private Duration currentEntryTTL;
	private final ConcurrentHashMap<K, CacheEntry<V>> cache;
	private final ScheduledExecutorService cleaner;
	private final AtomicReference<CacheEntry<V>> currentEntry = new AtomicReference<>();
	private final Clock clock;

	public ConcurrentCache(
		Duration cacheEntryTTL,
		Duration currentEntryTTL,
		ScheduledExecutorService cleaner,
		Clock clock) {
		this.cleaner = cleaner;
		this.clock = clock;
		this.cacheEntryTTL = cacheEntryTTL;
		this.currentEntryTTL = currentEntryTTL;
		cache = new ConcurrentHashMap<>();
		cleaner.scheduleAtFixedRate(this::evictExpired, cacheEntryTTL.toMillis(), cacheEntryTTL.toMillis(), TimeUnit.MILLISECONDS);
	}

	public ConcurrentCache(long ttl, TimeUnit unit, ScheduledExecutorService cleaner, Clock clock) {
		this(
			Duration.ofMillis(unit.toMillis(ttl)),
			Duration.ofMillis(unit.toMillis(ttl)),
			cleaner,
			clock);
	}

	public void setCacheEntryTTL(Duration cacheEntryTTL) {
		this.cacheEntryTTL = cacheEntryTTL;
	}

	public void setCurrentEntryTTL(Duration currentEntryTTL) {
		this.currentEntryTTL = currentEntryTTL;
	}

	public Duration getCacheEntryTTL() {
		return cacheEntryTTL;
	}

	public Duration getCurrentEntryTTL() {
		return currentEntryTTL;
	}

	public V put(K key, V value) {
		cache.put(key, new CacheEntry<>(value));

		return value;
	}

	public V get(K key) {
		CacheEntry<V> entry = cache.get(key);
		if (entry == null || isExpired(entry)) {
			return null;
		}
		entry.updateAccessTime();
		return entry.value;
	}

	public V putCurrent(K key, V value) {
		CacheEntry<V> entry = new CacheEntry<>(value);
		cache.put(key, entry);
		currentEntry.set(entry);

		return value;
	}

	public V getCurrent() {
		CacheEntry<V> entry = currentEntry.get();
		if (entry == null || isDead(entry)) {
			return null;
		}
		return entry.value;
	}

	private boolean isDead(CacheEntry<V> entry) {
		return isDead(clock.millis(), entry.birth, currentEntryTTL.toMillis());
	}

	private boolean isDead(long now, long birth, long ttlMillis) {
		return (now - birth) > ttlMillis;
	}

	boolean isExpired(long now, long lastAccessTime, long ttlMillis) {
		return (now - lastAccessTime) > ttlMillis;
	}

	private boolean isExpired(CacheEntry<V> entry) {
		return isExpired(clock.millis(), entry.lastAccessTime, cacheEntryTTL.toMillis());
	}

	private long evictExpired() {
		long evictionCount = 0;

		try {
			for (Map.Entry<K, CacheEntry<V>> e : cache.entrySet()) {
				if (isExpired(e.getValue())) {
					if (e.getValue().value instanceof AutoCloseable closeable) {
						closeable.close();
					}
					cache.remove(e.getKey());
					evictionCount++;
				}
			}
		}
		catch (Exception e) {
			return -1;
		}

		return evictionCount;
	}

	public void shutdown() {
		cleaner.shutdown();
	}

	public void clear() {
		for (Map.Entry<K, CacheEntry<V>> e: cache.entrySet()) {
			if (e.getValue().value instanceof AutoCloseable closeable) {
				try {
					closeable.close();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}

		cache.clear();
		currentEntry.set(null);
	}

	private class CacheEntry<T> {
		final T value;
		volatile long lastAccessTime;
		volatile long birth;

		CacheEntry(T v) {
			value = v;
			lastAccessTime = birth = ConcurrentCache.this.clock.millis();
		}

		void updateAccessTime() {
			lastAccessTime = ConcurrentCache.this.clock.millis();
		}
	}
}
