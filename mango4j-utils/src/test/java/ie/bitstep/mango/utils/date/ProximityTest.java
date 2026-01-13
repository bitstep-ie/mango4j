package ie.bitstep.mango.utils.date;

import ie.bitstep.mango.utils.date.Proximity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ProximityTest {

	@Test
	void elapsedInstants0() {
		Instant now = Instant.now();
		Proximity p = Proximity.of(now, now);

		assertThat(p.elapsedMillis()).isZero();
	}

	@Test
	void elapsedDates0() {
		Date now = new Date();
		Proximity p = Proximity.of(now, now);

		assertThat(p.elapsedMillis()).isZero();
	}

	@Test
	void testGetDifferenceBetweenDatesInDays() {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		long millis1 = System.currentTimeMillis();
		long millis2 = millis1;

		c1.setTimeInMillis(millis1);
		c2.setTimeInMillis(millis2);

		assertThat(Proximity.of(c1.getTime(), c2.getTime()).elapsedDays()).isZero();

		millis2 = millis1 + TimeUnit.DAYS.toMillis(2);
		c2.setTimeInMillis(millis2);
		assertThat(Proximity.of(c1.getTime(), c2.getTime()).elapsedDays()).isEqualTo(2);

		millis2 = millis1 - TimeUnit.DAYS.toMillis(2);
		c2.setTimeInMillis(millis2);
		assertThat(Proximity.of(c1.getTime(), c2.getTime()).elapsedDays()).isEqualTo(-2);
	}

	@Test
	void testGetDifferenceBetweenInstantsInDays() {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		long millis1 = System.currentTimeMillis();
		long millis2 = millis1;

		c1.setTimeInMillis(millis1);
		c2.setTimeInMillis(millis2);
		assertThat(Proximity.of(c1.toInstant(), c2.toInstant()).elapsedDays()).isZero();

		millis2 = millis1 + TimeUnit.DAYS.toMillis(2);
		c2.setTimeInMillis(millis2);
		assertThat(Proximity.of(c1.toInstant(), c2.toInstant()).elapsedDays()).isEqualTo(2);

		millis2 = millis1 - TimeUnit.DAYS.toMillis(2);
		c2.setTimeInMillis(millis2);
		assertThat(Proximity.of(c1.toInstant(), c2.toInstant()).elapsedDays()).isEqualTo(-2);
	}

	@Test
	void elapsedMillis() {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		long millis1 = System.currentTimeMillis();
		long millis2 = millis1 - 2;
		c1.setTimeInMillis(millis1);
		c2.setTimeInMillis(millis2);

		assertThat(Proximity.of(c1.toInstant(), c2.toInstant()).elapsedMillis()).isEqualTo(-2);
	}

	@Test
	void elapsedSeconds() {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		long millis1 = System.currentTimeMillis();
		long millis2 = millis1 - TimeUnit.SECONDS.toMillis(2);
		c1.setTimeInMillis(millis1);
		c2.setTimeInMillis(millis2);

		assertThat(Proximity.of(c1.toInstant(), c2.toInstant()).elapsedSeconds()).isEqualTo(-2);
	}

	@Test
	void elapsedMinutes() {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		long millis1 = System.currentTimeMillis();
		long millis2 = millis1 - TimeUnit.MINUTES.toMillis(2);
		c1.setTimeInMillis(millis1);
		c2.setTimeInMillis(millis2);

		assertThat(Proximity.of(c1.toInstant(), c2.toInstant()).elapsedMinutes()).isEqualTo(-2);
	}

	@Test
	void elapsedHours() {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		long millis1 = System.currentTimeMillis();
		long millis2 = millis1 - TimeUnit.HOURS.toMillis(2);
		c1.setTimeInMillis(millis1);
		c2.setTimeInMillis(millis2);

		assertThat(Proximity.of(c1.toInstant(), c2.toInstant()).elapsedHours()).isEqualTo(-2);
	}

	@Test
	void elapsedDays() {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		long millis1 = System.currentTimeMillis();
		long millis2 = millis1 - TimeUnit.DAYS.toMillis(2);
		c1.setTimeInMillis(millis1);
		c2.setTimeInMillis(millis2);

		assertThat(Proximity.of(c1.toInstant(), c2.toInstant()).elapsedDays()).isEqualTo(-2);
	}
}
