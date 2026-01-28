package ie.bitstep.mango.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MapUtils {
	/**
	 * Prevents instantiation.
	 */
	private MapUtils() { // NOSONAR
		// SONAR, add private constructor
	}

	/**
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param m The map to process
	 * @return Map with original elements wrapped in a List
	 *
	 * <pre>eg.{@code
	 * Map&lt;String, String&gt; input = MapBuilder.&lt;String, String&gt;map()
	 * 	.with("name", "java");
	 * Map&lt;String, List&lt;String&gt;&gt; output = MapUtils.&lt;String, String&gt;map()
	 * 	.entriesToList(input);
	 * }</pre>
	 */
	public static <K, V> Map<K, List<V>> entriesToList(Map<K, V> m) {
		return m.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> arrayWrap(e.getValue())));
	}

	/**
	 * Take an object of type V and return an array with that object as the only element
	 *
	 * @param e The object
	 * @return List<V>(e)</V></B>
	 */
	private static <V> List<V> arrayWrap(V e) {
		List<V> array = new ArrayList<>();

		array.add(e);

		return array;
	}

	/**
	 * Replace elements in target map with elements from source map, ignoring elements that do not exist
	 * in the target map
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param source the source
	 * @param target the target
	 * @return the replaced map
	 */
	public static <K, V> Map<K, V> replace(Map<K, V> source, Map<K, V> target) {
		return (Map<K, V>) MapUtilsInternal.<K, V>immutableCopy((Map<Object, Object>) source, (Map<Object, Object>) target, false);
	}

	/**
	 * Merge the source map into the target maps in a left-to-right order such that the resulting map
	 * contains all the elements from all the maps in a left-to-right order
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param source the source
	 * @param targets the targets
	 * @return the merged map
	 */
	public static <K, V> Map<K, V> merge(Map<K, V> source, Map... targets) {
		Map<K, V> result = copy(source);

		if (targets != null) {
			for (Map<K, V> target : targets) {
				result = (Map<K, V>) MapUtilsInternal.<K, V>immutableCopy((Map<Object, Object>) result, (Map<Object, Object>) target, true);
			}
		}

		return result;
	}

	/**
	 * Make a copy of a map
	 *
	 * @param <K> the key type
	 * @param <V> the value type
	 * @param source the source
	 * @return the copied map
	 */
	public static <K, V> Map<K, V> copy(Map<K, V> source) {
		return (Map<K, V>) MapUtilsInternal.<K, V>immutableCopy((Map<Object, Object>) source, null, true);
	}

	/**
	 * Adds key/value pairs to the map in order.
	 *
	 * @param m the map to update
	 * @param keyValuePairs alternating key/value entries
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return the updated map
	 * @throws IllegalArgumentException when the pair count is odd or the map is null
	 */
	public static <K, V> Map<K, V> put(Map<K, V> m, Object... keyValuePairs) {
		if (m != null && keyValuePairs.length % 2 == 0) {
			int index = 0;
			while (index < keyValuePairs.length) {
				m.put((K) keyValuePairs[index++], (V) keyValuePairs[index++]);
			}
		} else {
			throw new IllegalArgumentException();
		}

		return m;
	}

	/**
	 * Get a leaf node from a map
	 * <p>
	 * eg. MapUtils&lt;String, Object&gt;.getPath(m, "services", "pg", "plan")
	 *
	 * @param m the map
	 * @param path path to the node
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return the leaf node
	 */
	public static <K, V> Map<K, V> getPath(Map<K, V> m, String... path) {
		return MapUtilsInternal.getPath(m, false, Arrays.stream(path).iterator());
	}

	/**
	 * Creates a nested path in the map, inserting missing nodes.
	 *
	 * @param m the map to modify
	 * @param path the path keys
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return the map at the target path
	 */
	public static <K, V> Map<K, V> createPath(Map m, String... path) { // NOSONAR - provide parameterized type
		return MapUtilsInternal.getPath(m, true, Arrays.stream(path).iterator());
	}
}
