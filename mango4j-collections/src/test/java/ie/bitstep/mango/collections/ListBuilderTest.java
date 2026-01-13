package ie.bitstep.mango.collections;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListBuilderTest {
	@Test
	void testList() {
		List<String> list = ListBuilder.<String>list().build();

		assertThat(list).isInstanceOf(ArrayList.class)
			.isEmpty();
	}

	@Test
	void testListWithArray() {
		String[] a = {"The", "cow", "jumped", "over", "the", "moon"};
		List<String> list = ListBuilder.<String>list().add(a).build();

		assertThat(list).isInstanceOf(ArrayList.class)
			.hasSize(a.length);
	}

	@Test
	void testListWithCollection() {
		List<String> a = Arrays.asList("The", "cow", "jumped", "over", "the", "moon");
		List<String> list = ListBuilder.<String>list().add(a).build();

		assertThat(list).isInstanceOf(ArrayList.class)
			.hasSize(a.size());
	}

	@Test
	void testListWithImplementation() {
		List<String> list = ListBuilder.<String>list(new LinkedList<String>()).build();

		assertThat(list).isInstanceOf(LinkedList.class)
			.isEmpty();
	}

	@Test
	void testWith() {
		List<String> l = ListBuilder.<String>list()
			.add("Darthvader")
			.build();

		assertThat(l)
			.hasSize(1)
			.containsExactly("Darthvader");
	}

	@Test
	void testWithNestedList() {
		List l = ListBuilder.<String>list()
			.add("Tom")
			.add("Cruise")
			.build();

		assertThat(l)
			.hasSize(2)
			.containsExactly("Tom", "Cruise");
	}
}