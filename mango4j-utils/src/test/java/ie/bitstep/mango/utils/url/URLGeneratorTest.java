package ie.bitstep.mango.utils.url;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class URLGeneratorTest {

	@Test
	void testToString() {
		String expectedUrl = "http://localhost:8080/";

		assertThat(URLGenerator.ofEmpty().host("localhost").port(8080)).hasToString(expectedUrl);
	}

	@Test
	void testURI() {
		String expectedUrl = "http://localhost:8080/";

		assertThat(URLGenerator.ofEmpty().host("localhost").port(8080).toURI()).hasToString(expectedUrl);
	}

	@Test
	void testUrlGeneratorPathsAndParams() {
		String expectedUrl = "http://api.stage.bitstep.com/mdes/consumer/allocate?validFrom=11%2F22&validTo=02%2F23&singleUse=true&merchantLocked=true&limit=100";
		URLGenerator urlGenerator = URLGenerator.ofURL("http://api.stage.bitstep.com//mdes/consumer//")
			.path("//allocate//")
			.param("validFrom", "11/22")
			.param("validTo", "02/23")
			.param("singleUse", "true")
			.param("merchantLocked", "true")
			.param("limit", "100");
		String url = urlGenerator.toString();

		assertThat(url).isEqualTo(expectedUrl);
	}

	@Test
	void testUrlWithPathsWithEncodedChars() {
		String expectedUrl = "http://api.stage.bitstep.com/allocate+vcn";
		URLGenerator urlGenerator = URLGenerator.ofURL(expectedUrl);
		String url = urlGenerator.toString();

		assertThat(url).isEqualTo(expectedUrl);
	}

	@Test
	void testUrlWithHostWithEncodedChars() {
		String expectedUrl = "http://api.stage.master+card.com/allocate+vcn";

		Exception e = assertThrows(Exception.class, () -> URLGenerator.ofURL(expectedUrl));

		assertThat(e.getCause().getMessage()).isEqualTo("Invalid hostname: http://api.stage.master+card.com/allocate+vcn");
	}

	@Test
	void testUrlWithRandomScheme() {
		String expectedUrl = "mc://api.stage.bitstep.com/allocate+vcn";
		URLGenerator urlGenerator = URLGenerator.ofURL(expectedUrl);
		String url = urlGenerator.toString();

		assertThat(url).isEqualTo(expectedUrl);
	}

	@Test
	void testUrlGeneratorWithTrailingSlash() {
		String expectedUrl = "http://localhost:8080/";
		URLGenerator urlGenerator = URLGenerator.ofEmpty().host("localhost").port(8080);
		String actualUrl = urlGenerator.toString();

		assertThat(actualUrl).isEqualTo(expectedUrl);
	}

	@Test
	void testUrlGeneratorPortNullTest() {
		String expectedUrl = "http://localhost/";
		URLGenerator urlGenerator = URLGenerator.ofEmpty().host("localhost");
		String actualUrl = urlGenerator.toString();

		assertThat(actualUrl).isEqualTo(expectedUrl);
	}

	void testOfUrl(String expectedUrl) {
		assertThat(URLGenerator.ofURL(expectedUrl)).hasToString(expectedUrl);
	}

	void testOfURI(URI uri) {
		String expectedUrl = uri.normalize().toString();
		assertThat(URLGenerator.ofURI(uri)).hasToString(expectedUrl);
	}

	@Test
	void testUrlGeneratorOfURI() {
		testOfURI(URI.create("http://localhost/api/v1"));
	}

	@Test
	void testUrlGeneratorWithParams() {
		testOfUrl("http://localhost?name=hello");
	}

	@Test
	void testUrlGeneratorSchemePortNullTest() {
		testOfUrl("http://localhost/");
	}

	@Test
	void testUrlGeneratorofURL() {
		testOfUrl("http://localhost:4096/hello");
	}

	@Test
	void testUrlGeneratorofURLWithParams() {
		String expectedUrl = "http://localhost:4096/hello?name=bitstep&greeting=hello";
		URLGenerator urlGenerator = URLGenerator.ofURL(expectedUrl);
		String actualUrl = urlGenerator.toString();

		assertThat(actualUrl).isEqualTo(expectedUrl);
	}

	@Test
	void testUrlGeneratorofURLWithDuplicateParams() {
		String url = "http://localhost:4096/hello?name=bitstep&greeting=hello&greeting=goodbye";
		URLGenerator urlGenerator = URLGenerator.ofURL(url);
		String actualUrl = urlGenerator.toString();

		assertThat(actualUrl).isEqualTo(url);
	}

	@Test
	void testBaseUrlWithPath() {
		String expectedUrl = "http://localhost:4096/crqs";
		String baseUrl = "http://localhost:4096/";
		URLGenerator urlGenerator = URLGenerator.ofURL(baseUrl).path("crqs");
		String actualUrl = urlGenerator.toString();

		assertThat(actualUrl).isEqualTo(expectedUrl);
	}

}
