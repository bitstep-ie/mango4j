package ie.bitstep.mango.crypto;

import java.time.Duration;

import static java.lang.Math.round;
import static java.lang.String.format;

public record RetryConfiguration(int maxAttempts, Duration backoffDelay, float backOffMultiplier) {

	public static final Duration DEFAULT_BACKOFF_DELAY = Duration.ofMillis(100);

	public RetryConfiguration {
		if (maxAttempts < 1) {
			throw new IllegalArgumentException(format("maxAttempts (%s) must be greater than 0", maxAttempts));
		}

		if (backoffDelay == null) {
			backoffDelay = DEFAULT_BACKOFF_DELAY;
		} else if (backoffDelay.isNegative()) {
			throw new IllegalArgumentException(format("backoffDelay (%s) must not be negative", backoffDelay));
		}

		if (backOffMultiplier < 0) {
			throw new IllegalArgumentException(format("backOffMultiplier (%s) cannot be negative", backOffMultiplier));
		} else if (backOffMultiplier > 0) {
			backOffMultiplier = round(backOffMultiplier * 10) / 10.0f; // round to one decimal place
		}
	}
}