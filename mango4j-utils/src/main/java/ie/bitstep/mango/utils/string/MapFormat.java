package ie.bitstep.mango.utils.string;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * <p>
 * <code>MapFormat</code> takes a map of objects, formats them, then
 * inserts the formatted strings into the pattern at the appropriate places.
 * </p>
 *
 * @see java.text.MessageFormat for details of Patterns and Their Interpretation.
 * <p>
 * This code is heavily based on java.text.MessageFormat.
 * </p>
 */

@SuppressWarnings({"squid:S109"})
public class MapFormat {
	private static final String[] C_TYPE_LIST = {"", "", "number", "", "date", "", "time", ""};
	private static final String[] C_MODIFIER_LIST = {"", "", "currency", "", "percent", "", "integer"};
	private static final String[] C_DATE_MODIFIER_LIST = {"", "", "short", "", "medium", "", "long", "", "full"};

	private final List<Argument> arguments = new ArrayList<>();

	/**
	 * The locale to use for formatting numbers and dates.
	 */
	private Locale locale;

	/**
	 * The TimeZone to use for formatting dates.
	 */
	private TimeZone timeZone;

	/**
	 * The string that the formatted values are to be plugged into.  In other words, this
	 * is the pattern supplied on construction with all of the {} expressions taken out.
	 */
	private CharSequence baseTemplate;

	/**
	 * Constructs a MapFormat for the default locale and the
	 * specified pattern.
	 * The constructor first sets the locale, then parses the pattern and
	 * creates a list of subformats for the format elements contained in it.
	 * Patterns and their interpretation are specified in the
	 * <a href="#patterns">class description</a>.
	 *
	 * @param pattern the pattern for this message format
	 * @throws IllegalArgumentException if the pattern is invalid
	 */
	public MapFormat(String pattern) {
		this(pattern, Locale.getDefault(), TimeZone.getDefault());
	}

	/**
	 * Constructs a MapFormat for the specified locale and default timezone.
	 *
	 * @param pattern the pattern for this message format
	 * @param locale  the locale for this message format
	 */
	public MapFormat(String pattern, Locale locale) {
		this(pattern, locale, TimeZone.getDefault());
	}

	/**
	 * Constructs a MapFormat for the specified locale and
	 * pattern.
	 * The constructor first sets the locale, then parses the pattern and
	 * creates a list of subformats for the format elements contained in it.
	 * Patterns and their interpretation are specified in the
	 * <a href="#patterns">class description</a>.
	 *
	 * @param pattern  the pattern for this message format
	 * @param locale   the locale for this message format
	 * @param timezone the timezone for this message format
	 * @throws IllegalArgumentException if the pattern is invalid
	 */
	public MapFormat(String pattern, Locale locale, TimeZone timezone) {
		setLocale(locale);
		setTimeZone(timezone);
		applyPattern(pattern);
	}

	/**
	 * Sets the locale to be used when creating or comparing subformats.
	 * This affects subsequent calls to the {@link #applyPattern applyPattern}
	 *
	 * @param locale the locale to be used when creating or comparing subformats
	 */
	public final void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * Returns the current locale.
	 *
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Sets the timezone used for formatting dates.
	 *
	 * @param tz the timezone to use
	 */
	public final void setTimeZone(TimeZone tz) {
		timeZone = tz;
	}

	/**
	 * Returns the current timezone used for formatting.
	 *
	 * @return the timezone
	 */
	public TimeZone getTimeZone() {
		return timeZone;
	}

	/**
	 * Sets the pattern used by this message format.
	 * The method parses the pattern and creates a list of subformats
	 * for the format elements contained in it.
	 * Patterns and their interpretation are specified in the
	 * <a href="#patterns">class description</a>.
	 *
	 * @param pattern the pattern for this message format
	 * @throws IllegalArgumentException if the pattern is invalid
	 */
	private void applyPattern(CharSequence pattern) { // NOSONAR: Cyclomatic complexity
		StringBuilder[] segments = new StringBuilder[4];

		for (int i = 0; i < segments.length; i++) {
			segments[i] = new StringBuilder();
		}

		if (pattern != null) {
			int part = 0;
			int braceStack = 0;
			for (int i = 0; i < pattern.length(); i++) {
				char ch = pattern.charAt(i);
				if (ch == '\\') {
					segments[part].append(pattern.charAt(++i)); // NOSONAR
				} else if (part == 0) {
					part = firstPart(ch, part, segments);
				} else {
					switch (ch) {
						case ',':
							part = handleComma(part, segments, ch);
							break;
						case '{':
							braceStack = handleOpenBrace(braceStack, ch, segments[part]);
							break;
						case '}':
							if (braceStack == 0) {
								part = 0;
								makeFormat(segments);
							} else {
								--braceStack;
								segments[part].append(ch);
							}
							break;
						default:
							segments[part].append(ch);
							break;
					}
				}
			}

			if (braceStack == 0 && part != 0) {
				throw new IllegalArgumentException("Unmatched braces in the pattern [" + pattern + "]");
			}
		}

		baseTemplate = segments[0];
	}

	/**
	 * Handles an opening brace within a format segment.
	 *
	 * @param braceStack current brace depth
	 * @param ch the character processed
	 * @param segments the current segment builder
	 * @return the updated brace depth
	 */
	private static int handleOpenBrace(int braceStack, char ch, StringBuilder segments) {
		++braceStack;
		segments.append(ch);
		return braceStack;
	}

	/**
	 * Handles a comma separator within a format segment.
	 *
	 * @param part the current part index
	 * @param segments the segment builders
	 * @param ch the character processed
	 * @return the updated part index
	 */
	private static int handleComma(int part, StringBuilder[] segments, char ch) {
		if (part < 3) {
			part += 1;
		} else {
			segments[part].append(ch);
		}
		return part;
	}

	/**
	 * Handles the first part of a format segment.
	 *
	 * @param ch the character processed
	 * @param part the current part index
	 * @param segments the segment builders
	 * @return the updated part index
	 */
	private static int firstPart(char ch, int part, StringBuilder[] segments) {
		if (ch == '{') {
			part = 1;
		} else {
			segments[part].append(ch);
		}
		return part;
	}


	/**
	 * Formats a string using a map of named arguments.
	 *
	 * @param map the values map
	 * @return the formatted string
	 */
	public String format(Map<String, Object> map) {
		CharSequence result = subformat(map);
		return result.toString();
	}

	private static class Argument {
		private String iName;   // The name(key) of the string in the map
		private Format iFormat; // The Formatter used to process this argument
		private int iOffset;    // The Offset in iPattern
	}

	/**
	 * Internal routine used by format.
	 *
	 * @throws IllegalArgumentException if an argument in the
	 *                                  <code>values</code> array is not of the type
	 *                                  expected by the format element(s) that use it.
	 */
	@SuppressWarnings({"squid:S1871"})
	private CharSequence subformat(Map<String, Object> values) {
		// note: this implementation assumes a fast substring & index.
		// if this is not true, would be better to append chars one by one.

		StringBuilder result = new StringBuilder();

		int lastOffset = 0;
		int maxOffset = arguments.size();
		for (int i = 0; i < maxOffset; i++) {
			Argument argument = arguments.get(i);
			result.append(baseTemplate.subSequence(lastOffset, argument.iOffset));
			lastOffset = argument.iOffset;
			String argumentName = argument.iName;
			if (!findValue(values, argumentName)) {
				result.append('{').append(argumentName).append('}');
				continue;
			}

			Object value = getValue(values, argumentName);
			String arg = null;
			Format subFormatter = null;

			if (value == null) {
				arg = "null";
			} else if (argument.iFormat != null) {
				subFormatter = argument.iFormat;
			} else if (value instanceof Integer) { // Try to format as an Integer
				subFormatter = NumberFormat.getIntegerInstance(locale);
			} else if (value instanceof Long) { // Try to format as an Long
				subFormatter = NumberFormat.getIntegerInstance(locale);
			} else if (value instanceof Number) { // Try to format as a Number
				subFormatter = NumberFormat.getInstance(locale);
			} else if (value instanceof Date) { // Try to format as a Date
				subFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
				((SimpleDateFormat) subFormatter).setTimeZone(timeZone);
			} else if (value instanceof String x) {
				arg = x;
			} else {
				arg = value.toString();
			}

			if (subFormatter != null) {
				arg = subFormatter.format(value);
			}

			result.append(arg);
		}

		result.append(baseTemplate.subSequence(lastOffset, baseTemplate.length()));

		return result;
	}

	/**
	 * Resolves a nested value by dotted argument name.
	 *
	 * @param values the values map
	 * @param argumentName the argument name
	 * @return the resolved value
	 */
	private Object getValue(Map<String, Object> values, String argumentName) {
		return getValue(values, 0, argumentName.split("\\."));
	}

	/**
	 * Resolves a nested value by walking a path.
	 *
	 * @param values the values map
	 * @param offset current path offset
	 * @param argumentName the argument path segments
	 * @return the resolved value
	 */
	private Object getValue(Map<String, Object> values, int offset, String[] argumentName) {
		Object value = values.get(argumentName[offset]);

		if (value instanceof Map) {
			return getValue((Map<String, Object>) value, offset + 1, argumentName);
		}

		return value;
	}

	/**
	 * Checks whether a nested value exists for the argument name.
	 *
	 * @param values the values map
	 * @param argumentName the argument name
	 * @return true when present
	 */
	private boolean findValue(Map<String, Object> values, String argumentName) {
		if (values == null) {
			return false;
		}

		return findValue(values, 0, argumentName.split("\\."));
	}

	/**
	 * Checks whether a nested value exists for a path.
	 *
	 * @param values the values map
	 * @param offset current path offset
	 * @param argumentName the argument path segments
	 * @return true when present
	 */
	private boolean findValue(Map<String, Object> values, int offset, String[] argumentName) {
		if (!values.containsKey(argumentName[offset])) {
			return false;
		}

		Object value = values.get(argumentName[offset]);

		if (value instanceof Map) {
			return findValue((Map<String, Object>) value, offset + 1, argumentName);
		}

		return true;
	}

	/**
	 * Builds a format descriptor from parsed segments.
	 *
	 * @param segments the parsed segments
	 */
	@SuppressWarnings({"squid:S1871"})
	private void makeFormat(StringBuilder[] segments) {
		// get the argument name
		String argumentName = segments[1].toString();

		Argument argument = new Argument();
		arguments.add(argument);
		argument.iOffset = segments[0].length();
		argument.iName = argumentName;


		// now get the format
		Format newFormat = null;
		switch (findKeyword(segments[2].toString(), C_TYPE_LIST)) {
			case 0:
				break;
			case 1, 2: // number
				newFormat = switch (findKeyword(segments[3].toString(), C_MODIFIER_LIST)) {
					case 0 ->
						// default
							NumberFormat.getInstance(locale);
					case 1, 2 ->
						// currency
							NumberFormat.getCurrencyInstance(locale);
					case 3, 4 ->
						// percent
							NumberFormat.getPercentInstance(locale);
					case 5, 6 ->
						// integer
							NumberFormat.getIntegerInstance(locale);
					default ->
						// pattern
							new DecimalFormat(segments[3].toString(), new DecimalFormatSymbols(locale));
				};
				break;
			case 3, 4: // date
				newFormat = switch (findKeyword(segments[3].toString(), C_DATE_MODIFIER_LIST)) {
					case 0 ->
						// default
							DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
					case 1, 2 ->
						// short
							DateFormat.getDateInstance(DateFormat.SHORT, locale);
					case 3, 4 ->
						// medium
							DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
					case 5, 6 ->
						// long
							DateFormat.getDateInstance(DateFormat.LONG, locale);
					case 7, 8 ->
						// full
							DateFormat.getDateInstance(DateFormat.FULL, locale);
					default -> new SimpleDateFormat(segments[3].toString(), locale);
				};

				((SimpleDateFormat) newFormat).setTimeZone(timeZone);
				break;
			case 5, 6: // time
				newFormat = switch (findKeyword(segments[3].toString(), C_DATE_MODIFIER_LIST)) {
					case 0 ->
							// default
							DateFormat.getTimeInstance(DateFormat.DEFAULT, locale);
					case 1, 2 ->
							// short
							DateFormat.getTimeInstance(DateFormat.SHORT, locale);
					case 3, 4 ->
							// medium
							DateFormat.getTimeInstance(DateFormat.DEFAULT, locale);
					case 5, 6 ->
							// long
							DateFormat.getTimeInstance(DateFormat.LONG, locale);
					case 7, 8 ->
							// full
							DateFormat.getTimeInstance(DateFormat.FULL, locale);
					default -> new SimpleDateFormat(segments[3].toString(), locale);
				};

				((SimpleDateFormat) newFormat).setTimeZone(timeZone);
				break;
			default:
				throw new IllegalArgumentException("unknown format type at ");
		}
		argument.iFormat = newFormat;
		segments[1].setLength(0);   // throw away other segments
	}

	/**
	 * Finds a keyword index in a list of supported tokens.
	 *
	 * @param aString the input string
	 * @param list the supported keyword list
	 * @return the index or -1 when not found
	 */
	private static int findKeyword(String aString, String[] list) {
		String s = aString.trim().toLowerCase();
		for (int i = 0; i < list.length; i++) {
			if (s.equals(list[i])) {
				return i;
			}
		}
		return -1;
	}
}
