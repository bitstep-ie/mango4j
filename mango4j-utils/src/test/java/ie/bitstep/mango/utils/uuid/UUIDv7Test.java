package ie.bitstep.mango.utils.uuid;

import ie.bitstep.mango.utils.uuid.UUIDv7;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UUIDv7Test {

	@Mock
	private Clock mockClock;

	@Mock
	private SecureRandom mockRandom;

	private UUIDv7 generator;

	@BeforeEach
	void init() {
		generator = new UUIDv7(mockClock, mockRandom);
	}

	@Test
	void privateConstructor() throws Exception {
		Constructor<UUIDv7> constructor = UUIDv7.class.getDeclaredConstructor();
		constructor.setAccessible(true);

		assertThat(constructor.newInstance()).isInstanceOf(UUIDv7.class);
	}

	@Test
	void testGeneratesExpected() {
		given(mockClock.millis()).willReturn(1700000000000L);
		given(mockRandom.nextLong()).willReturn(10923874L);

		UUID uuid = generator.generate();

		assertThat(uuid).hasToString("018bcfe5-6800-7000-8000-000000a6af62");
	}

	@Test
	void testGeneratesValidUUID() {
		given(mockClock.millis()).willReturn(1700000000000L);
		given(mockRandom.nextLong()).willReturn(10923874L);

		UUID uuid = generator.generate();

		assertEquals(7, uuid.version());
		assertEquals(2, uuid.variant());
	}

	@Test
	void testTimestampIsEncodedCorrectly() {
		given(mockClock.millis()).willReturn(1700000000000L);
		given(mockRandom.nextLong()).willReturn(10923874L);

		UUID uuid = generator.generate();
		long timestamp = (uuid.getMostSignificantBits() >>> 16) & 0xFFFFFFFFFFFFL;

		assertEquals(1700000000000L, timestamp);
	}

	@Test
	void testLexicographicOrdering(@Mock Clock laterClock) {
		given(mockRandom.nextLong()).willReturn(10923874L);
		given(mockClock.millis()).willReturn(1700000000000L);
		given(laterClock.millis()).willReturn(1700000000500L);

		UUIDv7 g1 = new UUIDv7(mockClock, mockRandom);
		UUIDv7 g2 = new UUIDv7(laterClock, mockRandom);

		UUID u1 = g1.generate();
		UUID u2 = g2.generate();

		assertTrue(u1.compareTo(u2) < 0);
	}
}