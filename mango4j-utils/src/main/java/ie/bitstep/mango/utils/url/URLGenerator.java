package ie.bitstep.mango.utils.url;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * URLGenerator class
 * <p>
 * A handy class for managing the creation of URLs, helping eliminate
 * a series of common errors encountered when manipulating URLs
 * <p>
 * eg.
 * <pre>{@code
 * package ie.bitstep.mango.utils.url.examples;
 *
 * import ie.bitstep.mango.utils.url.URLGenerator;
 *
 * public class Test {
 *     public String getAllocateURL() {
 *         URLGenerator urlGenerator = URLGenerator.ofURL("http://api.stage.bitstep.ie/");
 *         urlGenerator.path("/mdes/");
 *         urlGenerator.path("consumer//");
 *         urlGenerator.path("//allocate//");
 *         urlGenerator.param("validFrom", "11/22");
 *         urlGenerator.param("validTo", "02/23");
 *         urlGenerator.param("singleUse", "true");
 *         urlGenerator.param("merchantLocked", "true");
 *         urlGenerator.param("limit", "100");
 *
 *         return urlGenerator.toString();
 *     }
 * }
 * }</pre>
 */
public class URLGenerator {
	public static final String HTTP = "http";
	public static final String HTTPS = "https";
	private String scheme = HTTP;
	private String host;
	private Integer port = -1;
	private List<String> pathComponents = new ArrayList<>();
	private HashMap<String, List<QueryParam>> parameterMap = new LinkedHashMap<>();

	/**
	 * Creates an empty URL generator.
	 */
	private URLGenerator() {
		// SONAR compliance
	}

	/**
	 * Create an empty URLGenerator
	 *
	 * @return An empty URLGenerator
	 */
	public static URLGenerator ofEmpty() {
		return new URLGenerator();
	}

	/**
	 * Create a URLGenerator from a URL
	 *
	 * @param url url A valid URL
	 * @return a URLGenerator representing the supplied URL
	 */
	public static URLGenerator ofURL(String url) {
		URLGenerator urlGenerator = new URLGenerator();
		URI uri = URI.create(url).normalize();

		if (uri.getHost() == null) {
			throw new IllegalArgumentException(new URISyntaxException(url, "Invalid hostname"));
		}

		urlGenerator.scheme(uri.getScheme())
				.host(uri.getHost())
				.port(uri.getPort());

		urlGenerator.initialisePath(uri);
		urlGenerator.initialiseQuery(uri);

		return urlGenerator;
	}

	/**
	 * Create a URLGenerator from a URI
	 *
	 * @param uri a URI object
	 * @return A URLGenerator representing the URI
	 */
	public static URLGenerator ofURI(URI uri) {
		return ofURL(uri.normalize().toString());
	}

	/**
	 * Return a URI equivalent of this URL
	 *
	 * @return A URI representing the current values in the URLGenerator
	 */
	public URI toURI() {
		return URI.create(toString()).normalize();
	}

	/**
	 * Return a string equivalent of this URL
	 *
	 * @return A string repsentation of this URLGenerator instance
	 */
	public String toString() {
		StringBuilder uri = new StringBuilder();

		uri.append(scheme).append("://").append(host);

		if (port != -1) {
			uri.append(":").append(port.toString());
		}

		for (String pathComponent : pathComponents) {
			uri.append("/").append(URLEncoder.encode(pathComponent, StandardCharsets.UTF_8));
		}

		if (!parameterMap.isEmpty()) {
			boolean firstParam = true;

			for (List<QueryParam> params : parameterMap.values()) {
				for (QueryParam param : params) {
					if (firstParam) {
						uri.append("?");
						firstParam = false;
					} else {
						uri.append("&");
					}

					uri.append(URLEncoder.encode(param.key(), StandardCharsets.UTF_8))
							.append("=")
							.append(URLEncoder.encode(param.value(), StandardCharsets.UTF_8));
				}
			}
		}

		if (pathComponents.isEmpty() && parameterMap.isEmpty()) {
			uri.append("/");
		}

		return uri.toString();
	}

	/**
	 * Set the scheme (http, or https)
	 * <p>
	 * See static vars
	 * HTTP
	 * HTTPS
	 *
	 * @param scheme The URI scheme (http:, https:, file:, mc: etc....)
	 * @return The URLGenerator the scheme() call was made on
	 */
	public URLGenerator scheme(String scheme) {
		this.scheme = scheme;
		return this;
	}

	/**
	 * Set the URL host, replaces any existing host definition
	 * <p>
	 * If the host has not been set by ofURL() or ofURI() then this will need to be called or the generated URL
	 * will be invalid.
	 *
	 * @param host The target host
	 * @return The URLGenerator the host() call was made on
	 */
	public URLGenerator host(String host) {
		this.host = host;
		return this;
	}

	/**
	 * Set the port for the URL (optional)
	 *
	 * @param port The target port (optional)
	 * @return The URLGenerator the port() call was made on
	 */
	public URLGenerator port(Integer port) {
		this.port = port;

		return this;
	}

	/**
	 * Sets a query parameter, will replace any existing parameter with the same key
	 *
	 * @param key   The parameter name
	 * @param value The parameter value
	 * @return The URLGenerator the param() call was made on
	 */
	public URLGenerator param(String key, String value) {
		parameterMap.computeIfAbsent(key, k -> newQueryParam()).add(new QueryParam(key, value));

		return this;
	}

	/**
	 * Creates a new list for query parameters.
	 *
	 * @return a new list instance
	 */
	private List<QueryParam> newQueryParam() {
		return new ArrayList<>();
	}

	/**
	 * Append a path to the current path
	 * <p>
	 * Splits current path by / and add all non-empty components to the path
	 * array.
	 *
	 * @param path The path you wish to append (eg. status, or /status/)
	 * @return The URLGenerator the path() call was made on
	 */
	public URLGenerator path(String path) {
		if (StringUtils.isNotBlank(path)) {
			pathComponents.addAll(Arrays.stream(path.split("/"))
					.sequential()
					.filter(StringUtils::isNotBlank)
					.map(e -> URLDecoder.decode(e, StandardCharsets.UTF_8))
					.toList());
		}

		return this;
	}

	/**
	 * Initializes the query parameters from a URI.
	 *
	 * @param uri the URI to parse
	 */
	private void initialiseQuery(URI uri) {
		String query = uri.getQuery();
		if (StringUtils.isNotBlank(query)) {
			List<String> queryParams = Arrays.stream(query.split("&"))
					.sequential()
					.filter(StringUtils::isNotBlank)
					.toList();

			for (String queryParam : queryParams) {
				QueryParam qp = QueryParam.create(queryParam);
				param(URLDecoder.decode(qp.key(), StandardCharsets.UTF_8), URLDecoder.decode(qp.value(), StandardCharsets.UTF_8));
			}
		}
	}

	/**
	 * Initializes the path from a URI.
	 *
	 * @param uri the URI to parse
	 */
	private void initialisePath(URI uri) {
		path(uri.getPath());
	}

}
