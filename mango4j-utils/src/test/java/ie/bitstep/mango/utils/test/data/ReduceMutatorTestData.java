package ie.bitstep.mango.utils.test.data;

import ie.bitstep.mango.utils.conformance.Reduce;

public class ReduceMutatorTestData {
	@Reduce(max = 6)
	public Boolean b;

	@Reduce(max = 8)
	public String s1 = "Mongo Database";

	@Reduce(max = 8, ellipsis = false)
	public String s2 = "Mongo Dat";

	@Reduce(max = 3, ellipsis = false)
	public String s3 = "Mongo Database";

	@Reduce(max = 4, ellipsis = true)
	public String s4 = "Mongo Database";

	@Reduce(max = 3, ellipsis = true)
	public String s5 = "Mon";

	@Reduce(max = 300, ellipsis = true)
	public String s6 = "PostgreSQL";

	@Reduce(max = 10, ellipsis = true)
	public String s7 = "PostgreSQL";

	@Reduce(max = 3, ellipsis = true)
	public String s8 = "Monday";
}
