package ie.bitstep.mango.utils.conformance;

import ie.bitstep.mango.utils.mutator.exceptions.MutatorException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConformerTest {

	@Test
	void privateConstructor() throws Exception {
		Constructor<Conformer> constructor = Conformer.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		assertThat(constructor.newInstance()).isInstanceOf(Conformer.class);
	}

	@Test
	void testReduceWith() throws MutatorException {
		ConformerTestClass ct = new ConformerTestClass(
			"standardX",
			"RelationalDatabase",
			"RelationalD",
			"This is a very long service description and should be Reduced",
			"",
			9,
			301,
			new ConformerTestCategory("SQL"));

		Conformer.conform(ct);

		assertThat(ct.getServiceName()).isEqualTo("standard");
		assertThat(ct.getPlan()).isEqualTo("Relational");
		assertThat(ct.getTag()).isEqualTo("Relation");
		assertThat(ct.getDescription()).isEqualTo("This is a very long service descripti...");
		assertThat(ct.getAmount()).isEqualTo(10);
		assertThat(ct.getTimeout()).isEqualTo(300);
	}

	@Test
	void testReduceDeep() throws MutatorException {
		ConformerTestClass ct = new ConformerTestClass(
			"standard",
			"RelationalDatabase",
			"RelationalDatabase",
			"This is a very long service description and should be Reduced",
			"",
			10,
			100,
			new ConformerTestCategory("SQL Database"));

		Conformer.deepConform(ct);

		assertThat(ct.getTag()).isEqualTo("Relation");
		assertThat(ct.getDescription()).isEqualTo("This is a very long service descripti...");
		assertThat(ct.getCategory().getCategory()).isEqualTo("SQL");
	}

	@Test
	void testTolerateMax() throws MutatorException {
		ConformerTestClass ct = new ConformerTestClass(
			"standard",
			"NoSQL",
			"standard-type-of-database",
			"This is a very long service description and should be Reduced",
			"",
			100,
			1000,
			new ConformerTestCategory("SQL"));

		Conformer.conform(ct);

		assertThat(ct.getTimeout()).isEqualTo(300);
		assertThat(ct.getPlan()).isEqualTo("standard-t");
	}

	@Test
	void testTolerateMin() throws MutatorException {
		ConformerTestClass ct = new ConformerTestClass(
			"standard",
			"NoSQL",
			"stand",
			"This is a very long service description and should be Reduced",
			"",
			100,
			1,
			new ConformerTestCategory("SQL"));

		Conformer.conform(ct);

		assertThat(ct.getTimeout()).isEqualTo(30);
		assertThat(ct.getPlan()).isEqualTo("stand ");
	}

	@Test
	void testTolerateInRange() throws MutatorException {
		ConformerTestClass ct = new ConformerTestClass(
			"standard",
			"NoSQL",
			"stand ",
			"This is a very long service description and should be Reduced",
			"",
			100,
			30,
			new ConformerTestCategory("SQL"));

		Conformer.conform(ct);

		assertThat(ct.getTimeout()).isEqualTo(30);
		assertThat(ct.getPlan()).isEqualTo("stand ");
	}

	@Test
	void testReduceNullValue() throws MutatorException {
		ConformerTestClass ct = new ConformerTestClass(
			"standard",
			"standard",
			"RelationalDatabase",
			null,
			"",
			10,
			100,
			new ConformerTestCategory("SQL"));

		Conformer.conform(ct);

		assertThat(ct.getDescription()).isNull();
	}

	@Test
	void testReduceFailNotString() {
		ReduceNotStringFail reduceNotStringFail = new ReduceNotStringFail();
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> Conformer.conform(reduceNotStringFail));

		assertThat(thrown.getMessage()).isEqualTo("java.lang.Integer");
	}
}
