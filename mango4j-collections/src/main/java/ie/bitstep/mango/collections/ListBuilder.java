package ie.bitstep.mango.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @param <V> The value type
 *            <p>
 *            See tests for example usage
 *            <p>
 */
public class ListBuilder<V> {
	private final List<V> l;

	/**
	 * Creates a builder backed by the supplied list implementation.
	 *
	 * @param list the list instance to populate
	 */
	public ListBuilder(List<V> list) {
		l = list;
	}

	/**
	 * Creates a builder backed by an {@link ArrayList}.
	 *
	 * @param <V> the element type
	 * @return a new builder
	 */
	public static <V> ListBuilder<V> list() {
		return new ListBuilder<>(new ArrayList<>());
	}

	/**
	 * Creates a builder backed by the supplied list implementation.
	 *
	 * @param implementation the list instance to populate
	 * @param <V> the element type
	 * @return a new builder
	 */
	public static <V> ListBuilder<V> list(List<V> implementation) {
		return new ListBuilder<>(implementation);
	}

	/**
	 * @param value The value
	 * @return <pre>eg.{@code
	 * List<String> list = ListBuilder.<String>list().add("Hello").build();
	 *         }</pre>
	 */
	public ListBuilder<V> add(V value) {
		l.add(value);
		return this;
	}

	/**
	 * @param values An array of value
	 * @return <pre>eg.{@code
	 * String[] a = {"The", "cow", "jumped", "over", "the", "moon"};
	 * List<String> list = ListBuilder.<String>list().add(a).build();
	 *         }</pre>
	 */
	public ListBuilder<V> add(V[] values) {
		Arrays.stream(values).iterator().forEachRemaining(l::add);
		return this;
	}

	/**
	 * Adds all elements in the collection.
	 *
	 * @param a the collection to add from
	 * @return this builder
	 */
	public ListBuilder<V> add(Collection<V> a) {
		a.stream().iterator().forEachRemaining(l::add);
		return this;
	}

	/**
	 * Returns the built list.
	 *
	 * @return the underlying list
	 */
	public List<V> build() {
		return l;
	}

}
