package ie.bitstep.mango.utils.uuid;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.UUID;

/**
 * UUIDv7 generator based on @see <a href="https://datatracker.ietf.org/doc/rfc9562/">UUIDv7 Specification</a>
 * Generates time-ordered UUIDs using millisecond precision and random entropy.
 */
public final class UUIDv7 {

	private final Clock clock;
	private final SecureRandom random;

	public UUIDv7() {
		this(Clock.systemUTC(), new SecureRandom());
	}

	public UUIDv7(Clock clock, SecureRandom random) {
		this.clock = clock;
		this.random = random;
	}

	public UUID generate() {
		long unixMillis = clock.millis();

		long msb = (unixMillis & 0xFFFFFFFFFFFFL) << 16;
		msb |= 0x7000L;

		long lsb = random.nextLong();
		lsb &= ~(0xC000000000000000L);
		lsb |= 0x8000000000000000L;

		return new UUID(msb, lsb);
	}
}