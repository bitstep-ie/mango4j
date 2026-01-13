package ie.bitstep.mango.utils.url;

public class QueryParam {
	private String key;
	private String value;

	public QueryParam(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public static QueryParam create(String s) {
		String[] parts = s.split("=");

		return new QueryParam(parts[0], parts[1]);
	}

	public String key() {
		return key;
	}

	public String value() {
		return value;
	}
}
