package ie.bitstep.mango.utils.date;

import ie.bitstep.mango.utils.date.CalendarUtils;
import ie.bitstep.mango.utils.date.DateUtils;
import ie.bitstep.mango.utils.exceptions.InvalidDateFormatException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 *
 */
@ExtendWith(MockitoExtension.class)
class DateUtilsTest {
	private final TimeZone tz = TimeZone.getTimeZone("UTC");
	private final Calendar now = Calendar.getInstance(tz);
	private final List<String> defaultDateFormats = Arrays.asList("yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss");
	private final List<String> expectedDateFormats = new ArrayList<>(defaultDateFormats);

	@Mock
	private MockedStatic<CalendarUtils> calendarUtilsMockedStatic;

	private Calendar invalidCalendar(Calendar calendar) {
		calendar.set(Calendar.DAY_OF_MONTH, 2);
		calendar.set(Calendar.HOUR_OF_DAY, 1);
		calendar.set(Calendar.MINUTE, 1);
		calendar.set(Calendar.SECOND, 1);
		calendar.set(Calendar.MILLISECOND, 1);

		return calendar;
	}

	@BeforeEach
	void setup() {
		TimeZone.setDefault(tz);
		now.set(Calendar.DAY_OF_MONTH, 1);
		now.set(Calendar.MONTH, 0);
		now.set(Calendar.DAY_OF_MONTH, 31);
	}

	@Test
	void getBeginningOfThisMonth() {
		Calendar c = Calendar.getInstance(tz);

		calendarUtilsMockedStatic.when(() -> CalendarUtils.getInstance(Mockito.any()))
			.thenReturn(Calendar.getInstance(tz));

		calendarUtilsMockedStatic.when(() -> CalendarUtils.clone(Mockito.any()))
			.thenAnswer(invocation -> {
				Calendar input = invocation.getArgument(0);
				return invalidCalendar(input); // your custom method
			});

		c.setTime(new Date(DateUtils.getBeginningOfThisMonth(tz).toEpochMilli()));

		Calendar expectedCalendar = Calendar.getInstance(tz);

		assertThat(c.get(Calendar.DAY_OF_MONTH)).withFailMessage("Day of the month").isEqualTo(1);
		assertThat(c.get(Calendar.HOUR_OF_DAY)).withFailMessage("Hour").isZero();
		assertThat(c.get(Calendar.MINUTE)).withFailMessage("Minute").isZero();
		assertThat(c.get(Calendar.SECOND)).withFailMessage("Second").isZero();
		assertThat(c.get(Calendar.MILLISECOND)).withFailMessage("Millisecond").isZero();
		assertThat(c.get(Calendar.MONTH)).withFailMessage("Month").isEqualTo(expectedCalendar.get(Calendar.MONTH));
		assertThat(c.get(Calendar.YEAR)).withFailMessage("Year").isEqualTo(expectedCalendar.get(Calendar.YEAR));
	}

	@Test
	void getEndOfThisMonth() {
		Calendar c = Calendar.getInstance(tz);

		calendarUtilsMockedStatic.when(() -> CalendarUtils.getInstance(Mockito.any()))
			.thenReturn(Calendar.getInstance(tz));

		calendarUtilsMockedStatic.when(() -> CalendarUtils.clone(Mockito.any()))
			.thenAnswer(invocation -> {
				Calendar input = invocation.getArgument(0);
				return invalidCalendar(input); // your custom method
			});

		Date date = new Date(DateUtils.getEndOfThisMonth(tz).toEpochMilli());
		c.setTime(date);

		Calendar expectedCalendar = Calendar.getInstance(tz);

		assertThat(c.get(Calendar.DAY_OF_MONTH)).isEqualTo(expectedCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		assertThat(c.get(Calendar.HOUR_OF_DAY)).withFailMessage("Hour").isEqualTo(23);
		assertThat(c.get(Calendar.MINUTE)).withFailMessage("Minute").isEqualTo(59);
		assertThat(c.get(Calendar.SECOND)).withFailMessage("Second").isEqualTo(59);
		assertThat(c.get(Calendar.MILLISECOND)).withFailMessage("Milliseconds: expected 999, got %d", Calendar.MILLISECOND).isEqualTo(999);
		assertThat(c.get(Calendar.MONTH)).withFailMessage("Month").isEqualTo(expectedCalendar.get(Calendar.MONTH));
		assertThat(c.get(Calendar.YEAR)).withFailMessage("Year").isEqualTo(expectedCalendar.get(Calendar.YEAR));
	}

	@Test
	void getBeginningOfMonths() {
		Calendar c = Calendar.getInstance(tz);

		calendarUtilsMockedStatic.when(() -> CalendarUtils.getInstance(Mockito.any()))
			.thenReturn(Calendar.getInstance(tz));

		calendarUtilsMockedStatic.when(() -> CalendarUtils.clone(Mockito.any()))
			.thenAnswer(invocation -> {
				Calendar input = invocation.getArgument(0);
				return invalidCalendar(input); // your custom method
			});


		for (int i = 0; i < 12; i++) {
			c.setTime(now.getTime());
			c.set(Calendar.MONTH, i);

			Calendar result = Calendar.getInstance(tz);
			result.setTime(new Date(DateUtils.getBeginningOfMonth(i, tz).toEpochMilli()));

			assertThat(result.get(Calendar.DAY_OF_MONTH)).withFailMessage("Day of the month").isEqualTo(1);
			assertThat(result.get(Calendar.HOUR_OF_DAY)).withFailMessage("Hour").isZero();
			assertThat(result.get(Calendar.MINUTE)).withFailMessage("Minute").isZero();
			assertThat(result.get(Calendar.SECOND)).withFailMessage("Second").isZero();
			assertThat(result.get(Calendar.MILLISECOND)).withFailMessage("Millisecond").isZero();
			assertThat(result.get(Calendar.MONTH)).withFailMessage("Month").isEqualTo(i);
		}
	}

	@Test
	void getBeginningOfMonth() {
		long mySuperMillSecondValue = 455L;
		Calendar mockCalendar = Mockito.mock(Calendar.class);
		Calendar mockCalendarClone = Mockito.mock(Calendar.class);

		calendarUtilsMockedStatic.when(() -> CalendarUtils.clone(mockCalendar))
			.thenReturn(mockCalendarClone);

		given(mockCalendarClone.getTimeInMillis()).willReturn(mySuperMillSecondValue);

		assertThat(DateUtils.getBeginningOfMonth(mockCalendar)).isEqualTo(Instant.ofEpochMilli(mySuperMillSecondValue));

		then(mockCalendarClone).should().set(Calendar.DAY_OF_MONTH, 1);
		then(mockCalendarClone).should().set(Calendar.HOUR_OF_DAY, 0);
		then(mockCalendarClone).should().set(Calendar.MINUTE, 0);
		then(mockCalendarClone).should().set(Calendar.SECOND, 0);
		then(mockCalendarClone).should().set(Calendar.MILLISECOND, 0);
	}

	@Test
	void getEndOfMonths() {
		calendarUtilsMockedStatic.when(() -> CalendarUtils.getInstance(Mockito.any()))
			.thenReturn(Calendar.getInstance(tz));

		calendarUtilsMockedStatic.when(() -> CalendarUtils.clone(Mockito.any()))
			.thenAnswer(invocation -> {
				Calendar input = invocation.getArgument(0);
				return invalidCalendar(input); // your custom method
			});

		for (int month = 0; month < 12; month++) {
			Calendar result = Calendar.getInstance(tz);
			result.setTimeInMillis(DateUtils.getEndOfMonth(month, tz).toEpochMilli());

			Calendar expectedCalendar = Calendar.getInstance(tz);

			assertThat(result.get(Calendar.MONTH)).withFailMessage("Month expected %d, got %d", month, result.get(Calendar.MONTH)).isEqualTo(month);
			assertThat(result.get(Calendar.DAY_OF_MONTH)).withFailMessage("Day of the month").isEqualTo(result.getActualMaximum(Calendar.DAY_OF_MONTH));
			assertThat(result.get(Calendar.HOUR_OF_DAY)).withFailMessage("Hour").isEqualTo(23);
			assertThat(result.get(Calendar.MINUTE)).withFailMessage("Minute").isEqualTo(59);
			assertThat(result.get(Calendar.SECOND)).withFailMessage("Second").isEqualTo(59);
			assertThat(result.get(Calendar.MILLISECOND)).withFailMessage("Millisecond").isEqualTo(999);
			assertThat(result.get(Calendar.YEAR)).withFailMessage("Year").isEqualTo(expectedCalendar.get(Calendar.YEAR));
		}
	}

	@Test
	void getEndOfMonthsByCalendar() {
		calendarUtilsMockedStatic.when(() -> CalendarUtils.getInstance(Mockito.any()))
			.thenReturn(Calendar.getInstance(tz));

		calendarUtilsMockedStatic.when(() -> CalendarUtils.clone(Mockito.any()))
			.thenAnswer(invocation -> {
				Calendar input = invocation.getArgument(0);
				return invalidCalendar(input); // your custom method
			});

		for (int month = 0; month < 12; month++) {
			Calendar result = Calendar.getInstance(tz);
			result.set(Calendar.MONTH, month);
			result.set(Calendar.DAY_OF_MONTH, result.getActualMaximum(Calendar.DAY_OF_MONTH) + 1); // one beyond the month to ensure getEndOfMonth() sets the day to 1, otherwise failure
			result.setTime(new Date(DateUtils.getEndOfMonth(result).toEpochMilli()));

			Calendar expectedCalendar = Calendar.getInstance(tz);

			assertThat(result.get(Calendar.MONTH)).withFailMessage("Month expected %d, got %d", month, result.get(Calendar.MONTH)).isEqualTo(month);
			assertThat(result.get(Calendar.DAY_OF_MONTH)).withFailMessage("Day of the month").isEqualTo(result.getActualMaximum(Calendar.DAY_OF_MONTH));
			assertThat(result.get(Calendar.HOUR_OF_DAY)).withFailMessage("Hour").isEqualTo(23);
			assertThat(result.get(Calendar.MINUTE)).withFailMessage("Minute").isEqualTo(59);
			assertThat(result.get(Calendar.SECOND)).withFailMessage("Second").isEqualTo(59);
			assertThat(result.get(Calendar.MILLISECOND)).withFailMessage("Milliseconds: expected 999, got %d", Calendar.MILLISECOND).isEqualTo(999);
			assertThat(result.get(Calendar.YEAR)).withFailMessage("Year").isEqualTo(expectedCalendar.get(Calendar.YEAR));
		}
	}

	@Test
	void getBeginingOfLastMonth() {
		Calendar c = Calendar.getInstance(tz);
		c.add(Calendar.MONTH, -1);
		Calendar result = Calendar.getInstance(tz);

		calendarUtilsMockedStatic.when(() -> CalendarUtils.getInstance(Mockito.any()))
			.thenReturn(Calendar.getInstance(tz));

		calendarUtilsMockedStatic.when(() -> CalendarUtils.clone(Mockito.any()))
			.thenAnswer(invocation -> {
				Calendar input = invocation.getArgument(0);
				return invalidCalendar(input); // your custom method
			});

		result.setTime(new Date(DateUtils.getBeginningOfLastMonth(tz).toEpochMilli()));

		assertThat(result.get(Calendar.DAY_OF_MONTH)).withFailMessage("Day of the month").isEqualTo(1);
		assertThat(result.get(Calendar.HOUR_OF_DAY)).withFailMessage("Hour").isZero();
		assertThat(result.get(Calendar.MINUTE)).withFailMessage("Minute").isZero();
		assertThat(result.get(Calendar.SECOND)).withFailMessage("Second").isZero();
		assertThat(result.get(Calendar.MILLISECOND)).withFailMessage("Millisecond").isZero();
		assertThat(result.get(Calendar.MONTH)).withFailMessage("Month").isEqualTo(c.get(Calendar.MONTH));
	}

	@Test
	void getEndOfLastMonth() {
		Calendar c = Calendar.getInstance(tz);
		c.add(Calendar.MONTH, -1);

		Calendar result = Calendar.getInstance(tz);

		calendarUtilsMockedStatic.when(() -> CalendarUtils.getInstance(Mockito.any()))
			.thenReturn(Calendar.getInstance(tz));

		calendarUtilsMockedStatic.when(() -> CalendarUtils.clone(Mockito.any()))
			.thenAnswer(invocation -> {
				Calendar input = invocation.getArgument(0);
				return invalidCalendar(input); // your custom method
			});

		result.setTime(new Date(DateUtils.getEndOfLastMonth(tz).toEpochMilli()));

		assertThat(result.get(Calendar.HOUR_OF_DAY)).withFailMessage("Hour").isEqualTo(23);
		assertThat(result.get(Calendar.MINUTE)).withFailMessage("Minute").isEqualTo(59);
		assertThat(result.get(Calendar.SECOND)).withFailMessage("Second").isEqualTo(59);
		assertThat(result.get(Calendar.MILLISECOND)).withFailMessage("Millisecond").isEqualTo(999);
		assertThat(result.get(Calendar.MONTH)).withFailMessage("Month").isEqualTo(c.get(Calendar.MONTH));
	}

	@Test
	void testNoDateFormats() {
		DateUtils dateUtils = new DateUtils();
		dateUtils.clearDateFormats();

		InvalidDateFormatException thrown = Assertions.assertThrows(InvalidDateFormatException.class, () -> {
			dateUtils.getDateInUTC("2019-04-02");
		});

		assertThat(thrown.getMessage()).isEqualTo("Invalid date format");
		assertThat(thrown.getFormats()).isEmpty();
	}

	@Test
	void testAdditionalDateFormat() {
		DateUtils dateUtils = new DateUtils();
		dateUtils.addDateFormat("dd-MM-yyyy HH:mm:ss");
		expectedDateFormats.add("dd-MM-yyyy HH:mm:ss");

		InvalidDateFormatException thrown = Assertions.assertThrows(InvalidDateFormatException.class, () -> {
			dateUtils.getDateInUTC("2019-04-XX");
		});

		assertThat(thrown.getMessage()).isEqualTo("Invalid date format");
		assertThat(thrown.getFormats()).isEqualTo(expectedDateFormats);
	}

	@Test
	void testRemoveDateFormat() {
		DateUtils dateUtils = new DateUtils();
		dateUtils.addDateFormat("dd-MM-yyyy HH:mm:ss");
		dateUtils.removeDateFormat("dd-MM-yyyy HH:mm:ss");

		InvalidDateFormatException thrown = Assertions.assertThrows(InvalidDateFormatException.class, () -> {
			dateUtils.getDateInUTC("2019-04-XX");
		});

		assertThat(thrown.getMessage()).isEqualTo("Invalid date format");
		assertThat(thrown.getFormats()).isEqualTo(expectedDateFormats);
	}

	@Test
	void testGetDateInUTC_Successful() {
		// Set timezone to something other than UTC before test
		TimeZone.setDefault(TimeZone.getTimeZone("Australia/Adelaide"));

		// verify that the timezone got changed
		assertThat(TimeZone.getDefault()).isEqualTo(TimeZone.getTimeZone("Australia/Adelaide"));

		try {
			DateUtils dateUtils = new DateUtils();
			dateUtils.getDateInUTC("2019-04-02");
			Date date1 = new Date(dateUtils.getDateInUTC("2019-04-02 11:20:59").toEpochMilli());
			Date date2 = new Date(dateUtils.getDateInUTC("2018-12-31 23:59:59").toEpochMilli());

			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(tz);
			calendar.setTime(date1);

			assertThat(calendar.get(Calendar.DAY_OF_MONTH)).withFailMessage("Day of the month").isEqualTo(2);
			assertThat(calendar.get(Calendar.HOUR)).withFailMessage("Hour").isEqualTo(11);
			assertThat(calendar.get(Calendar.MINUTE)).withFailMessage("Minute").isEqualTo(20);
			assertThat(calendar.get(Calendar.SECOND)).withFailMessage("Second").isEqualTo(59);
			assertThat(calendar.get(Calendar.MONTH)).withFailMessage("Month").isEqualTo(3);
			assertThat(calendar.get(Calendar.YEAR)).withFailMessage("Year").isEqualTo(2019);

			Calendar calendar2 = Calendar.getInstance();
			calendar2.setTimeZone(tz);
			calendar2.setTime(date2);

			assertThat(calendar2.get(Calendar.DAY_OF_MONTH)).withFailMessage("Day of the month").isEqualTo(31);
			assertThat(calendar2.get(Calendar.HOUR_OF_DAY)).withFailMessage("Hour").isEqualTo(23);
			assertThat(calendar2.get(Calendar.MINUTE)).withFailMessage("Minute").isEqualTo(59);
			assertThat(calendar2.get(Calendar.SECOND)).withFailMessage("Second").isEqualTo(59);
			assertThat(calendar2.get(Calendar.MONTH)).withFailMessage("Month").isEqualTo(11);
			assertThat(calendar2.get(Calendar.YEAR)).withFailMessage("Year").isEqualTo(2018);

		} catch (InvalidDateFormatException ex) {
			fail();
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"2019/04/02", "02-04-2019", "2019-14-19", "2019-03-39", "2019-03-19 55:09:00", "2019-04-02+0500"})
	void testInvalidDateFormats(String value) {
		DateUtils dateUtils = new DateUtils();

		InvalidDateFormatException thrown = Assertions.assertThrows(InvalidDateFormatException.class, () -> {
			dateUtils.getDateInUTC(value);
		});

		assertThat(thrown.getMessage()).isEqualTo("Invalid date format");
		assertThat(thrown.getFormats()).isEqualTo(expectedDateFormats);
	}


}
