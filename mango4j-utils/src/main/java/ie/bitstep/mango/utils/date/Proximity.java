package ie.bitstep.mango.utils.date;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Similar to the Java 8 Period class, but supports Date, and Instant rather than LocalDate
 */
public class Proximity {
	private long millis;

	/**
	 * Creates a Proximity instance using Instant objects.
	 *
	 * @param start The start Instant.
	 * @param end   The end Instant.
	 * @return A Proximity instance representing the time difference between the start and end Instants.
	 */
	public static Proximity of(Instant start, Instant end) {
		return new Proximity(start, end);
	}

	/**
	 * Creates a Proximity instance using Date objects.
	 *
	 * @param start The start Date.
	 * @param end   The end Date.
	 * @return A Proximity instance representing the time difference between the start and end Dates.
	 */
	public static Proximity of(Date start, Date end) {
		return new Proximity(start, end);
	}

	/**
	 * Constructor that calculates the time difference in milliseconds between two Date objects.
	 *
	 * @param start The start Date.
	 * @param end   The end Date.
	 */
	public Proximity(Date start, Date end) {
		millis = end.getTime() - start.getTime();
	}

	/**
	 * Constructor that calculates the time difference in milliseconds between two Instant objects.
	 *
	 * @param start The start Instant.
	 * @param end   The end Instant.
	 */
	public Proximity(Instant start, Instant end) {
		millis = end.toEpochMilli() - start.toEpochMilli();
	}

	/**
	 * Get the elapsed time in milliseconds.
	 *
	 * @return The elapsed time in milliseconds.
	 */
	public long elapsedMillis() {
		return millis;
	}

	/**
	 * Get the elapsed time in seconds.
	 *
	 * @return The elapsed time in seconds.
	 */
	public long elapsedSeconds() {
		return TimeUnit.MILLISECONDS.toSeconds(millis);
	}

	/**
	 * Get the elapsed time in minutes.
	 *
	 * @return The elapsed time in minutes.
	 */
	public long elapsedMinutes() {
		return TimeUnit.MILLISECONDS.toMinutes(millis);
	}

	/**
	 * Get the elapsed time in hours.
	 *
	 * @return The elapsed time in hours.
	 */
	public long elapsedHours() {
		return TimeUnit.MILLISECONDS.toHours(millis);
	}

	/**
	 * Get the elapsed time in days.
	 *
	 * @return The elapsed time in days.
	 */
	public long elapsedDays() {
		return TimeUnit.MILLISECONDS.toDays(millis);
	}
}
