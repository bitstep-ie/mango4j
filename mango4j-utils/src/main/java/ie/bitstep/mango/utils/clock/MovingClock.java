package ie.bitstep.mango.utils.clock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

public class MovingClock extends Clock {
	private final long incrementMillis;
	private final Clock refClock;
	private long offsetMs;

	public MovingClock(Clock refClock) {
		this(refClock, 200);
	}

	public MovingClock(Clock refClock, long incrementMillis) {
		this.refClock = refClock;
		this.incrementMillis = incrementMillis;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MovingClock that = (MovingClock) o;

		return Objects.equals(refClock, that.refClock) &&
			Objects.equals(offsetMs, that.offsetMs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(incrementMillis, refClock, incrementMillis);
	}

	public ZoneId getZone() {
		return refClock.getZone();
	}

	public Clock withZone(ZoneId zone) {
		throw new UnsupportedOperationException();
	}

	public Instant instant() {
		offsetMs += incrementMillis;
		return refClock.instant().plusMillis(offsetMs);
	}
}
