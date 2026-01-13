package ie.bitstep.mango.utils.string;

import ie.bitstep.mango.utils.string.MapFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapFormatTest {
	private TimeZone tz = TimeZone.getTimeZone("UTC");

	@BeforeEach
	void setup() {
		TimeZone.setDefault(TimeZone.getTimeZone("Australia/Sydney"));
	}

	@Test
	void setLocale() {
		MapFormat mapFormat = new MapFormat("Hello {firstname}, {lastname}", Locale.UK, tz);

		assertThat(mapFormat.getLocale()).isEqualTo(Locale.UK);

		mapFormat.setLocale(Locale.US);
		assertThat(mapFormat.getLocale()).isEqualTo(Locale.US);
	}

	@Test
	void setTimezone() {
		MapFormat mapFormat = new MapFormat("Hello {firstname}, {lastname}", Locale.UK, TimeZone.getTimeZone("UTC"));
		TimeZone newTZ = TimeZone.getTimeZone("CET");

		assertThat(mapFormat.getTimeZone()).isEqualTo(tz);

		mapFormat.setTimeZone(newTZ);
		assertThat(mapFormat.getTimeZone()).isEqualTo(newTZ);
	}

	@Test
	void formatLocale() {
		MapFormat mapFormat = new MapFormat("Hello {firstname}, {lastname}", Locale.UK, tz);
		Map<String, Object> map = new HashMap<>();
		map.put("firstname", "George");
		map.put("lastname", "Michael");
		String s = mapFormat.format(map);

		assertThat(s).isEqualTo("Hello George, Michael");
	}

	@Test
	void formatEmbeddedBrace() {
		MapFormat mapFormat = new MapFormat("Hello {firstname{}}, {lastname}", Locale.UK, tz);
		Map<String, Object> map = new HashMap<>();
		map.put("firstname", "George");
		map.put("lastname", "Michael");
		String s = mapFormat.format(map);

		assertThat(s).isEqualTo("Hello {firstname{}}, Michael");
	}

	@Test
	void formatNull() {
		MapFormat mapFormat = new MapFormat(null);
		Map<String, Object> params = new HashMap<>();

		long n = 1447675918849L; // 16th Nov 2015 12:11 UTC

		params.put("n", n);

		String result = mapFormat.format(params);
		assertThat(result).isEmpty();
	}

	@Test
	void formatUnknownType() {
		IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () ->
				new MapFormat("Hello {firstname,unknown}, {lastname,unknown}"));

		assertThat(illegalArgumentException.getMessage()).isEqualTo("unknown format type at ");
	}

	@Test
	void formatNested() {
		MapFormat mapFormat = new MapFormat("Hello {name.first}, {name.last}");
		Map<String, Object> profile = new HashMap<>();
		Map<String, Object> name = new HashMap<>();
		profile.put("name", name);
		name.put("first", "George");
		name.put("last", "Michael");
		String s = mapFormat.format(profile);

		assertThat(s).isEqualTo("Hello George, Michael");
	}

	@Test
	void formatNullData() {
		MapFormat mapFormat = new MapFormat("Hello {firstname}, {lastname}");
		String s = mapFormat.format(null);

		assertThat(s).isEqualTo("Hello {firstname}, {lastname}");
	}

	@Test
	void formatDecimal() {
		MapFormat mapFormat = new MapFormat("Total price: {price.total,number,#.##}");
		Map<String, Object> cart = new HashMap<>();
		Map<String, Object> price = new HashMap<>();
		cart.put("price", price);
		price.put("total", 238.456);
		String s = mapFormat.format(cart);

		assertThat(s).isEqualTo("Total price: 238.46");
	}

	@Test
	void formatCurrency() {
		MapFormat mapFormat = new MapFormat("Total price: {price.total,number,currency}", Locale.US);
		Map<String, Object> cart = new HashMap<>();
		Map<String, Object> price = new HashMap<>();
		cart.put("price", price);
		price.put("total", 238.456);
		String s = mapFormat.format(cart);

		assertThat(s).isEqualTo("Total price: $238.46");
	}

	@Test
	void formatCurrencyMalformedPattern() {
		IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () ->
				new MapFormat("Total price: {price.total,number,currency,}", Locale.US));

		assertThat(illegalArgumentException.getMessage()).isEqualTo("Malformed pattern \"currency,\"");
	}

	@Test
	void braceStackCloseMissing() {
		IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () ->
				new MapFormat("Hello {firstname"));

		assertThat(illegalArgumentException.getMessage()).isEqualTo("Unmatched braces in the pattern [Hello {firstname]");
	}

	@Test
	void braceStackDoubleOpen() {
		MapFormat mapFormat = new MapFormat("Hello {{", Locale.UK);
		Map<String, Object> map = new HashMap<>();
		map.put("firstname", "George");
		map.put("lastname", "Michael");
		String s = mapFormat.format(map);

		assertThat(s).isEqualTo("Hello ");
	}

	@Test
	void formatNumberAsPercent() {
		MapFormat mapFormat = new MapFormat("Total discount: {discount,number,percent}", Locale.US);
		Map<String, Object> values = new HashMap<>();
		values.put("discount", .4);
		String s = mapFormat.format(values);

		assertThat(s).isEqualTo("Total discount: 40%");
	}

	@Test
	void formatNumberAsInteger() {
		MapFormat mapFormat = new MapFormat("Total price: {price.total,number,integer}");
		Map<String, Object> cart = new HashMap<>();
		Map<String, Object> price = new HashMap<>();
		cart.put("price", price);
		price.put("total", Integer.valueOf(89));
		String s = mapFormat.format(cart);

		assertThat(s).isEqualTo("Total price: 89");
	}

	@Test
	void formatInteger() {
		MapFormat mapFormat = new MapFormat("Total price: {price.total}");
		Map<String, Object> cart = new HashMap<>();
		Map<String, Object> price = new HashMap<>();
		cart.put("price", price);
		price.put("total", Integer.valueOf(89));
		String s = mapFormat.format(cart);

		assertThat(s).isEqualTo("Total price: 89");
	}

	@Test
	void formatBigInteger() {
		MapFormat mapFormat = new MapFormat("Total price: {price.total}");
		Map<String, Object> cart = new HashMap<>();
		Map<String, Object> price = new HashMap<>();
		cart.put("price", price);
		price.put("total", BigInteger.valueOf(89));
		String s = mapFormat.format(cart);

		assertThat(s).isEqualTo("Total price: 89");
	}

	@Test
	void formatLong() {
		MapFormat mapFormat = new MapFormat("Total price: {price.total}");
		Map<String, Object> cart = new HashMap<>();
		Map<String, Object> price = new HashMap<>();
		cart.put("price", price);
		price.put("total", Long.valueOf(89));
		String s = mapFormat.format(cart);

		assertThat(s).isEqualTo("Total price: 89");
	}

	@Test
	void formatDefault() {
		MapFormat mapFormat = new MapFormat("Total price: {price.total,number}");
		Map<String, Object> cart = new HashMap<>();
		Map<String, Object> price = new HashMap<>();
		cart.put("price", price);
		price.put("total", 89);
		String s = mapFormat.format(cart);

		assertThat(s).isEqualTo("Total price: 89");
	}

	@Test
	void formatMissingKey() {
		MapFormat mapFormat = new MapFormat("Hello {name.firstname}, {name.lastname}");
		Map<String, Object> profile = new HashMap<>();
		Map<String, Object> name = new HashMap<>();
		profile.put("name", name);
		name.put("first", "George");
		name.put("last", "Michael");
		String s = mapFormat.format(profile);

		assertThat(s).isEqualTo("Hello {name.firstname}, {name.lastname}");
	}

	@Test
	void formatNullValue() {
		MapFormat mapFormat = new MapFormat("Hello {name.first}, {name.last}");
		Map<String, Object> profile = new HashMap<>();
		Map<String, Object> name = new HashMap<>();
		profile.put("name", name);
		name.put("first", null);
		name.put("last", null);
		String s = mapFormat.format(profile);

		assertThat(s).isEqualTo("Hello null, null");
	}

	@Test
	void formatEscaped() {
		MapFormat mapFormat = new MapFormat("Hello \\{{name.first}, {name.last}\\}");
		Map<String, Object> profile = new HashMap<>();
		Map<String, Object> name = new HashMap<>();
		profile.put("name", name);
		name.put("first", "George");
		name.put("last", "Micheal");
		String s = mapFormat.format(profile);

		assertThat(s).isEqualTo("Hello {George, Micheal}");
	}

	@Test
	void formatSimple() {
		MapFormat mapFormat = new MapFormat("[{thing}]");
		Map<String, Object> params = new HashMap<>();

		params.put("thing", new RuntimeException("Bad stuff happened"));

		String result = mapFormat.format(params);
		assertThat(result).isEqualTo("[java.lang.RuntimeException: Bad stuff happened]");
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
		"[{date,date}]|[16 Nov 2015]",
		"[{date}]|[16/11/2015, 12:11]",
		"[{date,date,dd/mm/yy HH:MM Z}]|[16/11/15 12:11 +0000]",
		"[{date,date,short}]|[16/11/2015]",
		"[{date,date,medium}]|[16 Nov 2015]",
		"[{date,date,long}]|[16 November 2015]",
		"[{date,date,full}]|[Monday, 16 November 2015]",
		"[{time,time,dd/mm/yy HH:MM Z}]|[16/11/15 12:11 +0000]",
		"[{time,time,short}]|[12:11]",
		"[{time,time}]|[12:11:58]",
		"[{time,time,short}]|[12:11]",
		"[{time,time,medium}]|[12:11:58]",
		"[{time,time,long}]|[12:11:58 UTC]",
		"[{time,time,full}]|[12:11:58 Coordinated Universal Time]"
	})
	void formatDatesTimes(String format, String expected) {
		Locale locale = Locale.UK;
		TimeZone timeZone = TimeZone.getTimeZone("UTC");
		MapFormat mapFormat = new MapFormat(format, locale, timeZone);
		Map<String, Object> params = new HashMap<>();

		long n = 1447675918849L; // 16th Nov 2015 12:11 UTC

		params.put("date", new Date(n));
		params.put("time", new Date(n));

		String result = mapFormat.format(params);
		assertThat(result).isEqualTo(expected);
		assertThat(mapFormat.getLocale()).isEqualTo(locale);
		assertThat(mapFormat.getTimeZone()).isEqualTo(timeZone);
	}
}