package ie.bitstep.mango.utils.clock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

public class MovingClock extends Clock {
	private final long incrementMillis;
	private final Clock refClock;
	private long offsetMs;

	/**
	 * Creates a moving clock with a default increment.
	 *
	 * @param refClock the reference clock
	 */
	public MovingClock(Clock refClock) {
		this(refClock, 200);
	}

	/**
	 * Creates a moving clock that advances by the given increment on each call.
	 *
	 * @param refClock the reference clock
	 * @param incrementMillis increment per call in milliseconds
	 */
	public MovingClock(Clock refClock, long incrementMillis) {
		this.refClock = refClock;
		this.incrementMillis = incrementMillis;
	}

	/**
	 * Compares clocks by reference clock and offset.
	 *
	 * @param o the other object
	 * @return true when equal
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MovingClock that = (MovingClock) o;

		return Objects.equals(refClock, that.refClock) &&
			Objects.equals(offsetMs, that.offsetMs);
	}

	/**
	 * Returns the hash code for this clock.
	 *
	 * @return the hash code
	 */
	@Override
	public int hashCode() {
		return Objects.hash(incrementMillis, refClock, incrementMillis);
	}

	/**
	 * Returns the time zone of the reference clock.
	 *
	 * @return the time zone
	 */
	public ZoneId getZone() {
		return refClock.getZone();
	}

	/**
	 * Not supported for this clock.
	 *
	 * @param zone the zone to apply
	 * @return never returns normally
	 */
	public Clock withZone(ZoneId zone) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the current instant and advances the offset.
	 *
	 * @return the current instant
	 */
	public Instant instant() {
		offsetMs += incrementMillis;
		return refClock.instant().plusMillis(offsetMs);
	}
}
