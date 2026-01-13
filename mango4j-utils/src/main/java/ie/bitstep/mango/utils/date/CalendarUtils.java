package ie.bitstep.mango.utils.date;

import java.util.Calendar;
import java.util.TimeZone;

public class CalendarUtils {
	private CalendarUtils() {
		// NOSONAR
	}

	public static Calendar getInstance(TimeZone tz) {
		return Calendar.getInstance(tz);
	}

	public static Calendar clone(Calendar calendar) {
		return (Calendar) calendar.clone();
	}
}
