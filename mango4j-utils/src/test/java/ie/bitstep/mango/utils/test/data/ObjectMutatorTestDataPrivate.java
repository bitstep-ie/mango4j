package ie.bitstep.mango.utils.test.data;

import ie.bitstep.mango.utils.mutator.annotations.Text;

public class ObjectMutatorTestDataPrivate {
	@Text
	private String greeting = "Hello<Freddy>";

	public String getGreeting() {
		return greeting;
	}
}
