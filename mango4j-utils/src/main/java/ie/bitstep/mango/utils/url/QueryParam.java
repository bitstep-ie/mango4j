package ie.bitstep.mango.utils.url;

public class QueryParam {
	private String key;
	private String value;

	/**
	 * Creates a query parameter with key and value.
	 *
	 * @param key the parameter key
	 * @param value the parameter value
	 */
	public QueryParam(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Parses a query parameter from a {@code key=value} string.
	 *
	 * @param s the string to parse
	 * @return the query parameter
	 */
	public static QueryParam create(String s) {
		String[] parts = s.split("=");

		return new QueryParam(parts[0], parts[1]);
	}

	/**
	 * Returns the parameter key.
	 *
	 * @return the key
	 */
	public String key() {
		return key;
	}

	/**
	 * Returns the parameter value.
	 *
	 * @return the value
	 */
	public String value() {
		return value;
	}
}
