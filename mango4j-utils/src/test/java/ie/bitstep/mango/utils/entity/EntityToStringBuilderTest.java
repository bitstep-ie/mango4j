package ie.bitstep.mango.utils.entity;

import ie.bitstep.mango.utils.entity.EntityToStringBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EntityToStringBuilderTest {

	static class TestChild {
		private String child = "child";

		public String toString() {
			return "child";
		}
	}

	static class TestClass {
		private int ii = 22;
		private String name = "name33";
		private BigDecimal amount = new BigDecimal("44.44");
		private Date date = new Date();
		private TestChild child = new TestChild();
		private List<TestChild> children = Collections.singletonList(new TestChild());
	}

	static class BadClass {
		private int $1 = 22;
		private String name = "name33";
		private BigDecimal amount = new BigDecimal("44.44");
		private Date date = new Date();
		private TestChild child = new TestChild();
		private List<TestChild> children = Collections.singletonList(new TestChild());
	}

	@Test
	void testToString() {
		TestClass obj = new TestClass();

		String objStr = EntityToStringBuilder.toString(obj);

		assertThat(objStr)
			.contains("ii=22")
			.contains("name=name33")
			.contains("amount=44.44")
			.contains("date=")
			.doesNotContain("child")
			.doesNotContain("children");
	}

	@Test
	void testBadClass() {
		BadClass obj = new BadClass();

		String objStr = EntityToStringBuilder.toString(obj);

		assertThat(objStr)
			.doesNotContain("$i")
			.contains("name=name33")
			.contains("amount=44.44")
			.contains("date=")
			.doesNotContain("child")
			.doesNotContain("children");
	}
}
