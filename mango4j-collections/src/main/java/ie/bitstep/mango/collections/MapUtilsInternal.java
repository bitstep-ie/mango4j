package ie.bitstep.mango.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapUtilsInternal {
	/**
	 * Prevents instantiation.
	 */
	private MapUtilsInternal() { // NOSONAR
		// SONAR
	}

	/**
	 * Creates an immutable-style copy by overlaying source onto target.
	 *
	 * @param source the source map
	 * @param target the target map to overlay onto
	 * @param injectMissing whether to add missing keys from source
	 * @return the merged copy
	 */
	static Map<Object, Object> immutableCopy(Map<Object, Object> source, Map<Object, Object> target, boolean injectMissing) {
		Map<Object, Object> copy = new LinkedHashMap<>();

		if (target != null && !target.isEmpty()) {
			// make a copy of the target
			for (Map.Entry<Object, Object> e : target.entrySet()) {
				overlay(e, copy, true);
			}
		}

		// copy the source to the copy
		for (Map.Entry<Object, Object> e : source.entrySet()) {
			overlay(e, copy, injectMissing);
		}

		return copy;
	}

	/**
	 * Overlays a single entry into the target.
	 *
	 * @param entry the entry to overlay
	 * @param target the target map
	 * @param injectMissing whether to add missing keys
	 */
	static void overlay(Map.Entry<Object, Object> entry, Map<Object, Object> target, boolean injectMissing) {
		if (target.containsKey(entry.getKey()) || injectMissing) {
			if (entry.getValue() instanceof Map) {
				doMapOverlay(entry, target, injectMissing);
			} else if (entry.getValue() instanceof List) {
				doListOverlay(entry, target, injectMissing);
			} else {
				target.put(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Overlays a list entry into the target map.
	 *
	 * @param entry the entry to overlay
	 * @param target the target map
	 * @param injectMissing whether to add missing keys
	 */
	static void doListOverlay(Map.Entry<Object, Object> entry, Map<Object, Object> target, boolean injectMissing) {
		if (target.containsKey(entry.getKey()) || injectMissing) {
			target.put(entry.getKey(), new ArrayList<Object>());
			copyTo((List<Object>) entry.getValue(), (List<Object>) target.get(entry.getKey()), injectMissing);
		}
	}

	/**
	 * Overlays a map entry into the target map.
	 *
	 * @param entry the entry to overlay
	 * @param target the target map
	 * @param injectMissing whether to add missing keys
	 */
	static void doMapOverlay(Map.Entry<Object, Object> entry, Map<Object, Object> target, boolean injectMissing) {
		if (!target.containsKey(entry.getKey()) && injectMissing) {
			target.put(entry.getKey(), new LinkedHashMap<Object, Object>());
		}

		copyTo((Map<Object, Object>) entry.getValue(), (Map<Object, Object>) target.get(entry.getKey()), injectMissing);
	}

	/**
	 * Copies a list, recursively cloning nested lists/maps.
	 *
	 * @param source the source list
	 * @param target the target list
	 * @param injectMissing whether to add missing keys in nested maps
	 * @return the target list
	 */
	static List<Object> copyTo(List<Object> source, List<Object> target, boolean injectMissing) {
		for (Object o : source) {
			if (o instanceof Map) {
				target.add(copyTo((Map<Object, Object>) o, new LinkedHashMap<>(), injectMissing));
			} else if (o instanceof List) {
				target.add(copyTo((List<Object>) o, new ArrayList<>(), injectMissing));
			} else {
				target.add(o);
			}

		}

		return target;
	}

	/**
	 * Copies a map, recursively cloning nested maps/lists.
	 *
	 * @param source the source map
	 * @param target the target map
	 * @param injectMissing whether to add missing keys
	 * @return the target map
	 */
	static Map<Object, Object> copyTo(Map<Object, Object> source, Map<Object, Object> target, boolean injectMissing) {
		for (Map.Entry<Object, Object> e : source.entrySet()) {
			overlay(e, target, injectMissing);
		}

		return target;
	}

	/**
	 * Gets a nested path from the map, optionally creating missing nodes.
	 *
	 * @param m the map to traverse
	 * @param create whether to create missing path nodes
	 * @param it iterator of path keys
	 * @param <K> the key type
	 * @param <V> the value type
	 * @return the map at the path
	 */
	static <K, V> Map<K, V> getPath(Map m, boolean create, Iterator<String> it) { // NOSONAR - provide parameterized type
		if (m != null) {
			if (!it.hasNext()) {
				return m;
			}

			String key = it.next();

			if (!m.containsKey(key)) {
				if (!create) {
					return null; // NOSONAR -= do not want to create missing paths
				}

				insertMapNode(m, key);
			}

			if (m.get(key) instanceof Map) {
				return getPath((Map<K, V>) m.get(key), create, it);
			} else {
				throw new IllegalArgumentException();
			}
		}

		throw new IllegalArgumentException();
	}

	/**
	 * Inserts an empty map node at the specified key.
	 *
	 * @param m the map to modify
	 * @param key the key to insert
	 * @param <K> the key type
	 * @param <V> the value type
	 */
	@SuppressWarnings("unchecked")
	static <K, V> void insertMapNode(Map m, String key) { // NOSONAR - provide parameterized type
		m.put(key, new LinkedHashMap<K, V>());
	}
}
