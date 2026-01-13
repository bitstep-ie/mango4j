package ie.bitstep.mango.utils.conformance;

import ie.bitstep.mango.utils.mutator.exceptions.MutatorException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TolerateMutatorTest {
	@Tolerate(min = 3, max = 6)
	public Boolean b = true;

	@Tolerate(min = 3, max = 6)
	public int x = 5;

	@Tolerate(min = 3, max = 6)
	public long z = 5;

	@Tolerate(min = 3, max = 6)
	public String y = "Hello";

	@Tolerate(min = 3, max = 6)
	public Float u = 2.3f;

	private Field findField(TolerateMutatorTest tolerateMutatorTest, String field) {
		return ReflectionUtils
			.findFields(tolerateMutatorTest.getClass(), f -> f.getName().equals(field), ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
			.stream().findFirst().orElse(null);
	}

	@Test
	void testTolerateLong() {
		Field f = findField(this, "z");
		Tolerate tolerate = f.getAnnotation(Tolerate.class);
		Long result;

		for (long index = tolerate.min() - 2; index <= tolerate.min(); index++) {
			z = index;
			result = (Long) new TolerateMutator().process(tolerate, z);
			assertThat(result).isEqualTo(tolerate.min());
		}

		for (long index = tolerate.max() + 2; index >= tolerate.max(); index--) {
			z = index;
			result = (Long) new TolerateMutator().process(tolerate, z);
			assertThat(result).isEqualTo(tolerate.max());
		}
	}

	@Test
	void testTolerateInteger() {
		Field f = findField(this, "x");
		Tolerate tolerate = f.getAnnotation(Tolerate.class);
		Integer result;

		for (int index = (int) tolerate.min() - 2; index <= (int) tolerate.min(); index++) {
			x = index;
			result = (Integer) new TolerateMutator().process(tolerate, x);
			assertThat(result).isEqualTo((int) tolerate.min());
		}

		for (int index = (int) tolerate.max() + 2; index >= (int) tolerate.max(); index--) {
			x = index;
			result = (Integer) new TolerateMutator().process(tolerate, x);
			assertThat(result).isEqualTo((int) tolerate.max());
		}
	}

	@Test
	void testTolerateUnknownType() {
		Field f = findField(this, "u");
		Tolerate tolerate = f.getAnnotation(Tolerate.class);
		TolerateMutator tolerateMutator = new TolerateMutator();

		IllegalArgumentException throwUnsupportedFloatType =  assertThrows(IllegalArgumentException.class, () ->
				tolerateMutator.process(tolerate, u)
		);

		assertThat(throwUnsupportedFloatType.getMessage()).isEqualTo("java.lang.Float");
	}

	@ParameterizedTest
	@ValueSource(strings = {"A very long string:A very", "A very:A very", "X:X  ", "X  :X  "})
	void testString(String s) {
		Field f = findField(this, "y");
		Tolerate tolerate = f.getAnnotation(Tolerate.class);
		String[] split = s.split(":");
		y = split[0];
		String result = (String) new TolerateMutator().process(tolerate, y);
		assertThat(result.length()).isBetween((int) tolerate.min(), (int) tolerate.max());
		assertThat(result).isEqualTo(split[1]);
	}

	@Test
	void tolerateIllegalAccess() {
		MutatorException expected = new MutatorException(new IllegalAccessException("class ie.bitstep.mango.reflection.accessors.PropertyAccessor cannot access a member of class ie.bitstep.mango.utils.conformance.TolerateMutatorTest with modifiers \"public\""));
		MutatorException mutatorException = assertThrows(MutatorException.class, () -> Conformer.conform(this));

		assertThat(mutatorException.getLocalizedMessage()).isEqualTo(expected.getLocalizedMessage());
		assertThat(mutatorException.getCause()).isInstanceOf(IllegalAccessException.class);
	}

	@Test
	void testAdd() {
		TolerateMutator tolerateMutator = new TolerateMutator();

		assertThat(tolerateMutator.add(1, 1)).isEqualTo(2);
		assertThat(tolerateMutator.add(2, -1)).isEqualTo(1);
	}
}
