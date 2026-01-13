package ie.bitstep.mango.utils.clock;

import ie.bitstep.mango.utils.clock.MovingClock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovingClockTest {
	@Mock
	private Clock clock;

	private final ZoneId zone = ZoneId.of("Europe/Dublin");

	@Test
	void testSameObjectEquals() {
		// GIVEN
		MovingClock movingClock1 = new MovingClock(clock);

		// WHEN

		// THEN
		assertThat(movingClock1.equals(movingClock1)).isTrue();
	}

	@Test
	void testNullEquals() {
		// GIVEN
		MovingClock movingClock1 = new MovingClock(clock);

		// WHEN

		// THEN
		assertThat(movingClock1.equals(null)).isFalse();  // NOSONAR, intentional comparison
	}

	@Test
	void testDifferentClassEquals() {
		// GIVEN
		MovingClock movingClock1 = new MovingClock(clock);

		// WHEN

		// THEN
		assertThat(movingClock1.equals("Hello")).isFalse(); // NOSONAR, intentional comparison  between different objects
	}

	@Test
	void testEquals() {
		MovingClock movingClock1 = new MovingClock(clock);
		MovingClock movingClock2 = new MovingClock(clock);

		assertThat(movingClock1).isEqualTo(movingClock2);
	}

	@Test
	void testNotEqualsClock() {
		// GIVEN
		Clock clock1 = Mockito.mock(Clock.class);
		Clock clock2 = Mockito.mock(Clock.class);
		MovingClock movingClock1 = new MovingClock(clock1);
		MovingClock movingClock2 = new MovingClock(clock2);

		// WHEN

		// THEN
		assertThat(movingClock1).isNotEqualTo(movingClock2);
	}

	@Test
	void testNotEqualsOffset() {
		// GIVEN
		MovingClock movingClock1 = new MovingClock(clock);
		MovingClock movingClock2 = new MovingClock(clock);

		// WHEN
		when(clock.instant()).thenReturn(Instant.now());
		movingClock1.instant();
		movingClock2.instant();
		movingClock2.instant();

		// THEN
		assertThat(movingClock1).isNotEqualTo(movingClock2);
	}

	@Test
	void testEqualsSameInstance() {
		MovingClock movingClock1 = new MovingClock(clock);

		// noinspection EqualsWithItself
		assertThat(movingClock1).isEqualTo(movingClock1);
	}

	@Test
	void testEqualsNull() {
		MovingClock movingClock1 = new MovingClock(clock);

		assertThat(movingClock1).isNotEqualTo(null);
	}

	@Test
	void testEqualsDifferentType() {
		MovingClock movingClock1 = new MovingClock(clock);

		assertThat(movingClock1).isNotEqualTo(new Object());
	}

	@Test
	void testEqualsDifferentIncrementInMillis() {
		MovingClock movingClock1 = new MovingClock(clock);
		MovingClock movingClock2 = new MovingClock(clock, 2000);

		assertThat(movingClock1).isEqualTo(movingClock2);
	}

	@Test
	void testHashCode() {
		// GIVEN
		MovingClock movingClock1 = new MovingClock(clock);
		MovingClock movingClock2 = new MovingClock(clock);

		// WHEN

		// THEN
		assertThat(movingClock1).hasSameHashCodeAs(movingClock2);
	}

	@Test
	void testHashCodeIncremwent() {
		// GIVEN
		MovingClock movingClock1 = new MovingClock(clock, 1000);
		MovingClock movingClock2 = new MovingClock(clock, 2000);

		// WHEN
		when(clock.instant()).thenReturn(Instant.now());
		movingClock1.instant();
		movingClock2.instant();

		// THEN
		assertThat(movingClock1.hashCode()).isNotEqualTo(movingClock2.hashCode());
	}

	@Test
	void testGetZone() {
		// GIVEN
		MovingClock movingClock = new MovingClock(clock);

		// WHEN
		when(clock.getZone()).thenReturn(zone);

		// THEN
		assertThat(movingClock.getZone()).isEqualTo(zone);
	}

	@Test
	void testWithZone() {
		// GIVEN
		MovingClock movingClock = new MovingClock(clock);

		// WHEN
		assertThrows(UnsupportedOperationException.class, () -> movingClock.withZone(zone));

		// THEN
	}

	@Test
	void testInstant() {
		// GIVEN
		MovingClock movingClock = new MovingClock(clock);
		Instant now = Instant.now();

		// WHEN
		when(clock.instant()).thenReturn(now);
		Instant instant1 = movingClock.instant();
		Instant instant2 = movingClock.instant();

		// THEN
		assertThat(instant2.toEpochMilli()).isGreaterThan(instant1.toEpochMilli());
	}


	@Test
	void testInstant443ms() {
		// GIVEN
		MovingClock movingClock = new MovingClock(clock, 443);
		Instant now = Instant.now();

		// WHEN
		when(clock.instant()).thenReturn(now);
		Instant instant1 = movingClock.instant();
		Instant instant2 = movingClock.instant();

		// THEN
		assertThat(instant2.toEpochMilli() - instant1.toEpochMilli()).isEqualTo(443);
	}

}