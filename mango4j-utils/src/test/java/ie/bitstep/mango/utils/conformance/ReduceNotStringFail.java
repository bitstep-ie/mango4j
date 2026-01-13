package ie.bitstep.mango.utils.conformance;

public class ReduceNotStringFail {
	@Reduce(max = 40)
	private Integer v = 20;

	public Integer getV() {
		return v;
	}

	public void setV(Integer v) {
		this.v = v;
	}

	public ReduceNotStringFail(ReduceNotStringFail r) {
		this.v = r.v;
	}

	public ReduceNotStringFail() {
	}
}
