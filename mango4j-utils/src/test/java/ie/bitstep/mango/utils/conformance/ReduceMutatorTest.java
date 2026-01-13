package ie.bitstep.mango.utils.conformance;

import ie.bitstep.mango.utils.test.data.ReduceMutatorTestData;
import ie.bitstep.mango.utils.mutator.classes.Derived;
import ie.bitstep.mango.utils.mutator.exceptions.MutatorException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReduceMutatorTest {
	ReduceMutatorTestData reduceMutatorTestData = new ReduceMutatorTestData();

	@Test
	void testReduceWithAbstractBase() {
		Derived derived = new Derived(
			"AX101233",
			"A very long error message, A very long error message",
			"AXON",
			"AX-UPC-0003");

		Derived expected = new Derived(
			"AX1012",
			"A very long error message",
			"AXON",
			"AX-UPC");

		Conformer.conform(derived);

		assertThat(derived).isEqualTo(expected);
	}

	@Test
	void reduceIllegalArgumentException() {
		reduceMutatorTestData.b = true;
		assertThrows(IllegalArgumentException.class, () -> Conformer.conform(reduceMutatorTestData));
	}

	@Test
	void reduce() throws MutatorException {
		Conformer.conform(reduceMutatorTestData);

		assertThat(reduceMutatorTestData.s1).isEqualTo("Mongo...");
		assertThat(reduceMutatorTestData.s2).isEqualTo("Mongo Da");
		assertThat(reduceMutatorTestData.s3).isEqualTo("Mon");
		assertThat(reduceMutatorTestData.s4).isEqualTo("M...");
		assertThat(reduceMutatorTestData.s5).isEqualTo("Mon");
		assertThat(reduceMutatorTestData.s6).isEqualTo("PostgreSQL");
		assertThat(reduceMutatorTestData.s7).isEqualTo("PostgreSQL");
	}

	@Test
	void testAdd() {
		ReduceMutator reduceMutator = new ReduceMutator();

		assertThat(reduceMutator.add(1, 1)).isEqualTo(2);
		assertThat(reduceMutator.add(2, -1)).isEqualTo(1);
	}
}
