package ie.bitstep.mango.collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConcurrentCacheTest {

	private ConcurrentCache<String, String> cache;

	private ConcurrentCache<String, AutoCloseable> closeableCache;

	private long now;

	@Mock
	private Clock clock;

	@Mock
	private ScheduledExecutorService cleaner;

	private static class CanClose implements AutoCloseable {

		@Override
		public void close() throws Exception {
			// NOSONAR: Test case support
		}
	}

	@Mock
	private CanClose canClose;

	@BeforeEach
	void setUp() {
		now = Instant.now().toEpochMilli();
		cache = new ConcurrentCache<>(2, TimeUnit.SECONDS, cleaner, clock); // TTL 2 seconds
		closeableCache = new ConcurrentCache<>(2, TimeUnit.SECONDS, cleaner, clock); // TTL 2 seconds
	}

	@AfterEach
	void tearDown() {
		cache.shutdown();
	}

	@Test
	void testSettingTheTTls() {
		cache = new ConcurrentCache<>(2, TimeUnit.SECONDS, cleaner, clock); // TTL 2 seconds

		assertThat(cache.getCacheEntryTTL()).isEqualTo(Duration.ofSeconds(2));
		assertThat(cache.getCurrentEntryTTL()).isEqualTo(Duration.ofSeconds(2));

		cache.setCacheEntryTTL(Duration.ofSeconds(250));
		cache.setCurrentEntryTTL(Duration.ofDays(7));

		assertThat(cache.getCacheEntryTTL()).isEqualTo(Duration.ofSeconds(250));
		assertThat(cache.getCurrentEntryTTL()).isEqualTo(Duration.ofDays(7));
	}

	@Test
	void testPutAndGet() {
		String s = "value1";
		assertThat(cache.put("key1", s)).isEqualTo(s);

		assertThat(cache.get("key1")).isEqualTo(s);
	}

	@Test
	void testShutdown() {
		// GIVEN

		// WHEN
		doNothing().when(cleaner).shutdown();

		// THEN
		cache.shutdown();

		verify(cleaner).shutdown();
	}

	@Test
	void testPutCurrentAndClear() {
		cache.putCurrent("key1", "value1");

		cache.clear();

		assertNull(cache.get("key1"));
		assertNull(cache.getCurrent());
	}

	@Test
	void testPutCurrentAndClearException() throws Exception {
		IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Failed!!");
		closeableCache.putCurrent("key1", canClose);

		doThrow(illegalArgumentException)
			.when(canClose)
			.close();

		RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
			closeableCache.clear();
		});


		assertThat(thrown.getCause()).isEqualTo(illegalArgumentException);
	}

	@Test
	void testGetRefreshesExpiry() {
		// GIVEN

		// WHEN
		when(clock.millis())
			.thenReturn(now)
			.thenReturn(now + 1500)
			.thenReturn(now + 3000)
		;

		// THEN
		cache.put("key2", "value2");

		assertEquals("value2", cache.get("key2")); // refreshes access time

		assertEquals("value2", cache.get("key2")); // should still be available
	}

	@Test
	void testIsExpired() {
		assertFalse(cache.isExpired(now, now, 0));
		assertFalse(cache.isExpired(now, now - 5, 5));
		assertTrue(cache.isExpired(now, now - 6, 5));
	}

	@Test
	void testNotExpireWithoutAccess() throws InterruptedException {
		// GIVEN

		// WHEN
		when(clock.millis())
			.thenReturn(now)
			.thenReturn(now + 1500)
		;

		// THEN
		cache.put("key3", "value3");

		Thread.sleep(1500); // NOSONAR
		assertThat(cache.get("key3")).isEqualTo("value3");
	}

	@Test
	void testPutCurrentAndGetCurrent() {
		String s = "currentValue";

		assertThat(cache.putCurrent("currentKey", s)).isEqualTo(s);

		assertDoesNotThrow(() -> {
			String current = cache.getCurrent();
			assertEquals(s, current);
		});
	}

	@Test
	void testGetCurrentThrowsExceptionOnExpiry() {
		// GIVEN

		// WHEN
		when(clock.millis())
			.thenReturn(now)
			.thenReturn(now + 2500)
		;

		// THEN
		cache.putCurrent("expiringKey", "expiringValue");

		assertNull(cache.getCurrent());
	}

	@Test
	void testMultiplePutsAndExpiryIndependently() {
		// GIVEN

		// WHEN
		when(clock.millis())
			.thenReturn(now)
			.thenReturn(now)
			.thenReturn(now)
			.thenReturn(now)
			.thenReturn(now)
			.thenReturn(now + 1500)
			.thenReturn(now + 2500)
		;

		cache.put("keyA", "A");
		cache.put("keyB", "B");
		cache.putCurrent("keyC", "C");

		assertEquals("A", cache.get("keyA")); // refreshes A
		assertDoesNotThrow(() -> assertEquals("C", cache.getCurrent())); // current still valid

		assertNull(cache.get("keyB")); // expired
	}

	@Test
	void testEvictExpiredManually() throws Exception {
		cache.put("keyX", "valueX");
		assertEquals("valueX", cache.get("keyX"));

		// Simulate time passing past TTL
		when(clock.millis())
			.thenReturn(now + 1000)
			.thenReturn(now + 3000)
		;

		cache.put("keyY", "valueY");
		cache.put("keyZ", "valueZ");

		// Call evictExpired() via reflection
		Method evictMethod = ConcurrentCache.class.getDeclaredMethod("evictExpired");
		evictMethod.setAccessible(true);
		long count = (long) evictMethod.invoke(cache);

		assertNull(cache.get("keyX"));
		assertThat(count).isEqualTo(1);
	}

	@Test
	void testEvictExpiredAutoCloseable() throws Exception {
		closeableCache.put("keyX", canClose);

		// Simulate time passing past TTL
		when(clock.millis())
			.thenReturn(now + 1000)
			.thenReturn(now + 3000)
		;

		// Call evictExpired() via reflection
		Method evictMethod = ConcurrentCache.class.getDeclaredMethod("evictExpired");
		evictMethod.setAccessible(true);
		long count = (long) evictMethod.invoke(closeableCache);

		verify(canClose).close();

		assertNull(closeableCache.get("keyX"));
		assertThat(count).isEqualTo(1);
	}

	@Test
	void testClearCacheAutoCloseable() throws Exception {
		closeableCache.put("keyX", canClose);

		closeableCache.clear();

		verify(canClose).close();

		assertNull(closeableCache.get("keyX"));
	}

	@Test
	@SuppressWarnings("unchecked")
	void testEvictExpiredCatchesException() throws Exception {
		// GIVEN
		Field cacheField = ConcurrentCache.class.getDeclaredField("cache");
		cacheField.setAccessible(true);

		// Insert a bad value into the map directly
		ConcurrentHashMap<String, ?> internalMap = (ConcurrentHashMap<String, ?>) cacheField.get(cache);
		// Intentionally insert an invalid object
		((ConcurrentHashMap<String, Object>) internalMap).put("badKey", "thisIsNotACacheEntry");

		// Call evictExpired() via reflection
		Method evictMethod = ConcurrentCache.class.getDeclaredMethod("evictExpired");
		evictMethod.setAccessible(true);
		long count = (long) evictMethod.invoke(cache);

		assertThat(count).isEqualTo(-1); // indicates eviction failure
	}
}
