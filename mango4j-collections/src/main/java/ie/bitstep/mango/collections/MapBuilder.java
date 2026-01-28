package ie.bitstep.mango.collections;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @param <K> The key type
 * @param <V> The value type
 *            <p>
 *            See tests for example usage
 *            <p>
 */
public class MapBuilder<K, V> {
	private final Map<K, V> m;

	/**
	 * Creates a builder backed by the supplied map implementation.
	 *
	 * @param map the map instance to populate
	 */
	public MapBuilder(Map<K, V> map) {
		m = map;
	}

	/**
	 * Creates a builder backed by a {@link LinkedHashMap}.
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new builder
	 */
	public static <K, V> MapBuilder<K, V> map() {
		return new MapBuilder<>(new LinkedHashMap<>());
	}

	/**
	 * Creates a builder backed by the supplied map implementation.
	 *
	 * @param implementation the map instance to populate
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return a new builder
	 */
	public static <K, V> MapBuilder<K, V> map(Map<K, V> implementation) {
		return new MapBuilder<>(implementation);
	}

	/**
	 * @param key   The key
	 * @param value The value
	 * @return
	 * <pre>eg.{@code
	 * Map<String, Object> m = MapBuilder.<String, Object>map(new TreeMap)
	 *         .with("pg",
	 *             MapBuilder.<String, String>map()
	 *                 .with("credentials",
	 *                     MapBuilder.<String, String>map()
	 *                         .with("userid", "hello2")
	 *                 )
	 *         ).with("redis",
	 *             MapBuilder.<String, String>map()
	 *                 .with("credentials",
	 *                     MapBuilder.<String, String>map()
	 *                         .with("userid", "hello3")
	 *                 )
	 *         ).build();
	 *     }
	 * </pre>
	 */
	public MapBuilder<K, V> with(K key, V value) {
		m.put(key, value);

		return this;
	}

	/**
	 * Create a path under this MapBuilder and return a MapBuilder to this path
	 *
	 * @param keys The keys
	 * @return
	 *
	 * <pre>eg.
	 *   <code>
	 *     MapBuilder&lt;String, Object&gt; mapBuilder = MapBuilder.&lt;String, Object&gt;map();
	 *
	 * 		mapBuilder
	 * 			.withPath("manifest", "services")
	 * 			.with("pg", "PostgreSQL")
	 * 			.with("redis", "Redis");
	 *
	 * 		mapBuilder
	 * 			.withPath("manifest", "services")
	 * 			.with("queue", "Axon");
	 *
	 * 		mapBuilder
	 * 			.withPath("manifest", "context")
	 * 			.with("platform", "MKS");
	 *
	 * 		Map map = mapBuilder.build();
	 * 	</code>
	 * </pre>
	 */
	public MapBuilder<K, V> withPath(String... keys) {
		return new MapBuilder<>(MapUtils.createPath(m, keys));
	}

	/**
	 * Copies all the key from the specified map to this MapBuilder, overwriting existing values
	 *
	 * @param data A map containing values to want to inject into this MapBuilder
	 * @return <pre>eg.{@code
	 * public Map<String, Object> services(Map<String, String> options) {
	 *     return MapBuilder.<String, Object>map(new TreeMap)
	 *         .with("pg",
	 *             MapBuilder.<String, String>map()
	 *                 .with("credentials",
	 *                     MapBuilder.<String, String>map()
	 *                         .with("userid", "hello2")
	 *                 )
	 *         ).with("redis",
	 *             MapBuilder.<String, String>map()
	 *                 .with("credentials",
	 *                     MapBuilder.<String, String>map()
	 *                         .with("userid", "hello3")
	 *                 )
	 *                 .inject(options)
	 *         ).build();
	 *     }
	 * }
	 * </pre>
	 */
	public MapBuilder<K, V> injectAll(Map<K, V> data) {
		m.putAll(data);

		return this;
	}

	/**
	 * Copies only the missing keys from the specified map to this MapBuilder
	 *
	 * @param data A map containing values to want to add into this MapBuilder, does not overwrite existing values
	 * @return <pre>eg.{@code
	 * public Map<String, Object> services(Map<String, String> options) {
	 *     return MapBuilder.<String, Object>map(new TreeMap)
	 *         .with("pg",
	 *             MapBuilder.<String, String>map()
	 *                 .with("credentials",
	 *                     MapBuilder.<String, String>map()
	 *                         .with("userid", "hello2")
	 *                 )
	 *         ).with("redis",
	 *             MapBuilder.<String, String>map()
	 *                 .with("credentials",
	 *                     MapBuilder.<String, String>map()
	 *                         .with("userid", "hello3")
	 *                 )
	 *                 .missing(options)
	 *         ).build();
	 *     }
	 * }
	 * </pre>
	 */
	public MapBuilder<K, V> injectMissing(Map<K, V> data) {
		for (Map.Entry<K, V> e : (data).entrySet()) {
			if (!m.containsKey(e.getKey())) {
				m.put(e.getKey(), e.getValue());
			}
		}

		return this;
	}

	/**
	 * Copies only the already existing keys from the specified map to this MapBuilder
	 *
	 * @param data A map containing values you want to update into this MapBuilder, does not insert missing values
	 * @return <pre>eg.{@code
	 * public Map<String, Object> services(Map<String, String> options) {
	 *     return MapBuilder.<String, Object>map(new TreeMap)
	 *         .with("pg",
	 *             MapBuilder.<String, String>map()
	 *                 .with("credentials",
	 *                     MapBuilder.<String, String>map()
	 *                         .with("userid", "hello2")
	 *                 )
	 *         ).with("redis",
	 *             MapBuilder.<String, String>map()
	 *                 .with("credentials",
	 *                     MapBuilder.<String, String>map()
	 *                         .with("userid", "hello3")
	 *                 )
	 *                 .update(options)
	 *         ).build();
	 *     }
	 * }
	 * </pre>
	 */
	public MapBuilder<K, V> updateExisting(Map<K, V> data) {
		for (Map.Entry<K, V> e : (data).entrySet()) {
			if (m.containsKey(e.getKey())) {
				m.put(e.getKey(), e.getValue());
			}
		}

		return this;
	}

	/**
	 * Returns the built map.
	 *
	 * @return the underlying map
	 */
	public Map<K, V> build() {
		return m;
	}
}
