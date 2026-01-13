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

	public ListBuilder(List<V> list) {
		l = list;
	}

	public static <V> ListBuilder<V> list() {
		return new ListBuilder<>(new ArrayList<>());
	}

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

	public ListBuilder<V> add(Collection<V> a) {
		a.stream().iterator().forEachRemaining(l::add);
		return this;
	}

	public List<V> build() {
		return l;
	}

}
