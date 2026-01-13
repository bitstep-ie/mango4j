package ie.bitstep.mango.utils.date;

import ie.bitstep.mango.utils.exceptions.InvalidDateFormatException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Utility class for working with dates and times.
 */
public class DateUtils {

	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private final Map<String, String> dateFormats = new LinkedHashMap<>();

	/**
	 * Constructs a DateUtils instance with default date formats.
	 */
	public DateUtils() {
		dateFormats.put(DATE_FORMAT, DATE_FORMAT);
		dateFormats.put(DATE_TIME_FORMAT, DATE_TIME_FORMAT);
	}

	/**
	 * Clears all custom date formats added to this DateUtils instance.
	 */
	public void clearDateFormats() {
		dateFormats.clear();
	}

	/**
	 * Adds a custom date format to this DateUtils instance.
	 *
	 * @param format The custom date format to add.
	 */
	public void addDateFormat(String format) {
		dateFormats.put(format, format);
	}

	/**
	 * Removes a custom date format from this DateUtils instance.
	 *
	 * @param format The custom date format to remove.
	 */
	public void removeDateFormat(String format) {
		dateFormats.remove(format);
	}

	// Methods for manipulating calendar and getting instants based on months

	/**
	 * Gets the beginning of the month for the provided Calendar instance.
	 *
	 * @param in The input Calendar.
	 * @return Instant representing the beginning of the month.
	 */
	public static Instant getBeginningOfMonth(Calendar in) {
		Calendar calendar = CalendarUtils.clone(in);

		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return Instant.ofEpochMilli(calendar.getTimeInMillis());
	}

	/**
	 * Gets the beginning of the specified month in the provided timezone.
	 *
	 * @param month The month (0-indexed).
	 * @param tz    The timezone.
	 * @return Instant representing the beginning of the specified month.
	 */
	public static Instant getBeginningOfMonth(int month, TimeZone tz) {
		Calendar calendar = Calendar.getInstance(tz);

		calendar.set(Calendar.MONTH, month);

		return getBeginningOfMonth(calendar);
	}

	/**
	 * @param tz timezone
	 * @return instant
	 */
	public static Instant getBeginningOfLastMonth(TimeZone tz) {
		Calendar calendar = CalendarUtils.getInstance(tz);

		calendar.add(Calendar.MONTH, -1);

		return getBeginningOfMonth(calendar);
	}

	/**
	 * @param tz timezone
	 * @return instant
	 */
	public static Instant getBeginningOfThisMonth(TimeZone tz) {
		Calendar calendar = Calendar.getInstance(tz);

		return getBeginningOfMonth(calendar.get(Calendar.MONTH), tz);
	}

	/**
	 * @param in Calendar
	 * @return instant
	 */
	public static Instant getEndOfMonth(Calendar in) {
		Calendar calendar = CalendarUtils.clone(in);

		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH) + 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		calendar.add(Calendar.MILLISECOND, -1);

		return Instant.ofEpochMilli(calendar.getTimeInMillis());
	}

	/**
	 * @param month month
	 * @param tz    timezone
	 * @return instant
	 */
	public static Instant getEndOfMonth(int month, TimeZone tz) {
		Calendar calendar = Calendar.getInstance(tz);

		calendar.set(Calendar.MONTH, month);

		return getEndOfMonth(calendar);
	}

	/**
	 * @param tz timezone
	 * @return instant
	 */
	public static Instant getEndOfLastMonth(TimeZone tz) {
		Calendar calendar = Calendar.getInstance(tz);

		calendar.add(Calendar.MONTH, -1);

		return getEndOfMonth(calendar);
	}

	/**
	 * @param tz timezone
	 * @return instant
	 */
	public static Instant getEndOfThisMonth(TimeZone tz) {
		return getEndOfMonth(Calendar.getInstance(tz));
	}

	public Instant getDateInUTC(String dateString) throws InvalidDateFormatException {
		for (String format : dateFormats.keySet()) {
			try {
				return getParsedDate(dateString, format);
			} catch (Exception e) {
				// NOSONAR
				// ignore exceptions
			}
		}

		throw new InvalidDateFormatException(dateFormats);
	}

	/**
	 * Parses the date string using the specified pattern and returns the corresponding Instant in UTC.
	 *
	 * @param dateString The input date string.
	 * @param pattern    The date pattern to use for parsing.
	 * @return Instant in UTC.
	 * @throws ParseException            If the date string cannot be parsed.
	 */
	private static Instant getParsedDate(String dateString, String pattern) throws ParseException {
		DateTimeFormatter.ofPattern(pattern).parse(dateString); // NOSONAR:force date to match pattern, as SimpleDateFormat is too lenient, even with lenient set to false
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		return Instant.ofEpochMilli(sdf.parse(dateString).getTime());
	}
}
