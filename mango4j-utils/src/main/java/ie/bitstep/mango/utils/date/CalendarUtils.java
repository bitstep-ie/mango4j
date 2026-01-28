package ie.bitstep.mango.utils.date;

import java.util.Calendar;
import java.util.TimeZone;

public class CalendarUtils {
	/**
	 * Prevents instantiation.
	 */
	private CalendarUtils() {
		// NOSONAR
	}

	/**
	 * Returns a calendar instance for the supplied timezone.
	 *
	 * @param tz the time zone
	 * @return a calendar instance
	 */
	public static Calendar getInstance(TimeZone tz) {
		return Calendar.getInstance(tz);
	}

	/**
	 * Clones a calendar instance.
	 *
	 * @param calendar the calendar to clone
	 * @return the cloned calendar
	 */
	public static Calendar clone(Calendar calendar) {
		return (Calendar) calendar.clone();
	}
}
