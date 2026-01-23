package ie.bitstep.mango.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Utilities for reconciling a "current" collection with a "desired" collection
 * based on a key extracted from each element.
 * <p>
 * Typical use case:
 * <ul>
 *   <li><b>current</b> – existing state (e.g. from DB or config)</li>
 *   <li><b>desired</b> – target state you want to apply</li>
 * </ul>
 * <p>
 * Semantics:
 * <ul>
 *   <li>For each element in {@code desired}:
 *     <ul>
 *       <li>If a matching key exists in {@code current}, the existing instance is kept.</li>
 *       <li>Otherwise, the element from {@code desired} is added.</li>
 *     </ul>
 *   </li>
 *   <li>Any element in {@code current} whose key does not appear in {@code desired} is removed.</li>
 *   <li>If {@code current} is an insertion-ordered collection (e.g. {@link java.util.List}, {@link java.util.LinkedHashSet}),
 *       its iteration order after reconciliation matches the iteration order of {@code desired}.</li>
 * </ul>
 * <p>
 * Keys must be unique within each side with respect to the given {@code keyExtractor}.
 */
public final class CollectionReconciler {

	/**
	 * Prevents instantiation.
	 */
	private CollectionReconciler() {
	// utility class
	}

	/**
	 * Reconcile {@code current} in-place so that, after this call, it contains exactly the
	 * elements from {@code desired} (by key).
	 *
	 * <p>The {@code current} collection is mutated and also returned for convenience.</p>
	 *
	 * @param current      the mutable collection to be reconciled (will be cleared and refilled)
	 * @param desired      the desired target elements
	 * @param keyExtractor function that extracts a key identifying each element
	 * @param <T>          element type
	 * @param <K>          key type
	 * @param <C>          collection type for the {@code current} side
	 * @return the same {@code current} instance, mutated to reflect {@code desired}
	 * @throws NullPointerException     if any argument, element, or key is null
	 * @throws IllegalArgumentException if keys are not unique within {@code current} or {@code desired}
	 */
	public static <T, K, C extends Collection<T>> C reconcile(
			C current,
			Iterable<? extends T> desired,
			Function<? super T, ? extends K> keyExtractor
	) {
		reconcileInternal(current, desired, keyExtractor, false);
		return current;
	}

	/**
	 * Reconcile {@code current} in-place and also return a {@link Result} describing
	 * which elements were added, removed, and kept.
	 *
	 * @param current      the mutable collection to be reconciled (will be cleared and refilled)
	 * @param desired      the desired target elements
	 * @param keyExtractor function that extracts a key identifying each element
	 * @param <T>          element type
	 * @param <K>          key type
	 * @param <C>          collection type for the {@code current} side
	 * @return a {@link Result} describing the reconciliation
	 * @throws NullPointerException     if any argument, element, or key is null
	 * @throws IllegalArgumentException if keys are not unique within {@code current} or {@code desired}
	 */
	public static <T, K, C extends Collection<T>> Result<T> reconcileAndReport(
			C current,
			Iterable<? extends T> desired,
			Function<? super T, ? extends K> keyExtractor
	) {
		return reconcileInternal(current, desired, keyExtractor, true);
	}

	/**
	 * Core reconciliation logic shared by both public methods.
	 *
	 * @return a {@link Result} if reporting is enabled, otherwise {@code null}.
	 */
	private static <T, K, C extends Collection<T>> Result<T> reconcileInternal(
			C current,
			Iterable<? extends T> desired,
			Function<? super T, ? extends K> keyExtractor,
			boolean report
	) {
		Objects.requireNonNull(current, "current");
		Objects.requireNonNull(desired, "desired");
		Objects.requireNonNull(keyExtractor, "keyExtractor");

// Index current by unique key (validates nulls + duplicates).
		Map<K, T> currentByKey = buildUniqueKeyMap(current, keyExtractor);

// Track desired keys to compute removals later.
		int expectedDesiredSize = calcInitialDesiredSize(desired);
		Set<K> desiredKeys = new HashSet<>(calcInitialKeySetCapacity(expectedDesiredSize));

// Initialize all lists via a single helper (Option B: mutable empty lists in no-report mode).
		Buckets<T> buckets = createBuckets(report, expectedDesiredSize);

// Single pass over desired: build merged order, added/kept, and track desired keys.
		for (T desiredElement : desired) {
			Objects.requireNonNull(desiredElement, "desired iterable contains null element");
			K key = Objects.requireNonNull(
					keyExtractor.apply(desiredElement),
					"keyExtractor returned null for desired element"
			);

// Ensure keys are unique in 'desired'.
			if (!desiredKeys.add(key)) {
				throw new IllegalArgumentException("Duplicate key in desired elements: " + key);
			}

			T existing = currentByKey.get(key);
			if (existing != null) {
				buckets.merged.add(existing);
				reportKept(buckets, existing, report);
			} else {
				buckets.merged.add(desiredElement);
				reportAdded(desiredElement, buckets, report);
			}
		}

// Compute 'removed' via set difference.
		for (Map.Entry<K, T> entry : currentByKey.entrySet()) {
			if (!desiredKeys.contains(entry.getKey())) {
				reportRemoved(entry, buckets, report);
			}
		}

// Mutate the original collection instance in-place to match desired order by key.
		current.clear();
		current.addAll(buckets.merged);

// Return result only when reporting is enabled.
		return buckets.toResultOrNull();
	}

	static int calcInitialKeySetCapacity(int expectedDesiredSize) {
		return Math.max(16, expectedDesiredSize * 2);
	}

	/**
	 * Determines a starting size for desired collections.
	 *
	 * @param desired the desired iterable
	 * @param <T> element type
	 * @return the expected size or a default
	 */
	static <T> int calcInitialDesiredSize(Iterable<? extends T> desired) {
		return (desired instanceof Collection<?> c) ? c.size() : 16;
	}

	/**
	 * Adds a removed element to the report bucket if enabled.
	 *
	 * @param entry the removed entry
	 * @param buckets result buckets
	 * @param report whether reporting is enabled
	 * @param <T> element type
	 * @param <K> key type
	 */
	private static <T, K> void reportRemoved(Map.Entry<K, T> entry, Buckets<T> buckets, boolean report) {
		if (report) {
			buckets.removed.add(entry.getValue());
		}
	}

	/**
	 * Adds an added element to the report bucket if enabled.
	 *
	 * @param desiredElement the element added
	 * @param buckets result buckets
	 * @param report whether reporting is enabled
	 * @param <T> element type
	 */
	private static <T> void reportAdded(T desiredElement, Buckets<T> buckets, boolean report) {
		if (report) {
			buckets.added.add(desiredElement);
		}
	}

	/**
	 * Adds a kept element to the report bucket if enabled.
	 *
	 * @param buckets result buckets
	 * @param existing the kept element
	 * @param report whether reporting is enabled
	 * @param <T> element type
	 */
	private static <T> void reportKept(Buckets<T> buckets, T existing, boolean report) {
		if (report) {
			buckets.kept.add(existing);
		}
	}

	/**
	 * Builds a key-indexed map, validating for nulls and duplicates.
	 *
	 * @param elements the elements to index
	 * @param keyExtractor the key extractor
	 * @param <T> element type
	 * @param <K> key type
	 * @return a map of keys to elements
	 */
	private static <T, K> Map<K, T> buildUniqueKeyMap(
			Collection<? extends T> elements,
			Function<? super T, ? extends K> keyExtractor
	) {
		Map<K, T> map = new HashMap<>(calcInitialSize(elements));
		for (T element : elements) {
			Objects.requireNonNull(element, "current collection contains null element");
			K key = Objects.requireNonNull(
					keyExtractor.apply(element),
					"keyExtractor returned null for current element"
			);
			if (map.putIfAbsent(key, element) != null) {
				throw new IllegalArgumentException("Duplicate key in current collection: " + key);
			}
		}
		return map;
	}

	/**
	 * Calculates an initial map capacity for the given collection.
	 *
	 * @param elements the elements to size for
	 * @param <T> element type
	 * @return the initial capacity
	 */
	static <T> int calcInitialSize(Collection<? extends T> elements) {
// Simple sizing heuristic: keep capacity ≥ 16 and > size to reduce early rehashing.
		return Math.max(16, elements.size() * 2);
	}

	/**
	 * Result of a reconciliation operation.
	 *
	 * @param <T> element type
	 */
	public static final class Result<T> {
		private final List<T> added;
		private final List<T> removed;
		private final List<T> kept;

		/**
		 * Creates a result snapshot.
		 *
		 * @param added items added during reconciliation
		 * @param removed items removed during reconciliation
		 * @param kept items retained during reconciliation
		 */
		private Result(List<T> added, List<T> removed, List<T> kept) {
			this.added = List.copyOf(added);
			this.removed = List.copyOf(removed);
			this.kept = List.copyOf(kept);
		}

		/**
		 * Returns items added during reconciliation.
		 *
		 * @return added items
		 */
		public List<T> getAdded() {
			return added;
		}

		/**
		 * Returns items removed during reconciliation.
		 *
		 * @return removed items
		 */
		public List<T> getRemoved() {
			return removed;
		}

		/**
		 * Returns items kept during reconciliation.
		 *
		 * @return kept items
		 */
		public List<T> getKept() {
			return kept;
		}

		/**
		 * Returns a string representation of the reconciliation result.
		 *
		 * @return the result string
		 */
		@Override
		public String toString() {
			return "Result{" +
					"added=" + added +
					", removed=" + removed +
					", kept=" + kept +
					'}';
		}
	}

	/**
	 * Buckets to hold all collections used during reconciliation.
	 * Lists are always non-null. When report == false, added/removed/kept are mutable empty lists.
	 */
	private static final class Buckets<T> {
		final List<T> merged;
		final List<T> added;
		final List<T> removed;
		final List<T> kept;
		final boolean report;

		/**
		 * Creates bucket lists for reconciliation.
		 *
		 * @param report whether reporting is enabled
		 * @param expectedDesiredSize expected desired collection size
		 */
		Buckets(boolean report, int expectedDesiredSize) {
			this.report = report;
// Pre-size merged to expected desired size to reduce reallocations.
			this.merged = new ArrayList<>(Math.max(16, expectedDesiredSize));
			if (report) {
				this.added = new ArrayList<>();
				this.removed = new ArrayList<>();
				this.kept = new ArrayList<>();
			} else {
				this.added = Collections.emptyList();
				this.removed = Collections.emptyList();
				this.kept = Collections.emptyList();
			}
		}

		/**
		 * Returns a {@link Result} if reporting is enabled.
		 *
		 * @return the result or null
		 */
		Result<T> toResultOrNull() {
			if (!report) return null;
			return new Result<>(added, removed, kept);
		}
	}

	/**
	 * Creates buckets sized for the desired collection.
	 *
	 * @param report whether reporting is enabled
	 * @param expectedDesiredSize expected desired collection size
	 * @param <T> element type
	 * @return new buckets instance
	 */
	private static <T> Buckets<T> createBuckets(boolean report, int expectedDesiredSize) {
		return new Buckets<>(report, expectedDesiredSize);
	}
}
