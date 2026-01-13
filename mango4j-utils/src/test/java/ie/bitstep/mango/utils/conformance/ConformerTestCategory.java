package ie.bitstep.mango.utils.conformance;

public class ConformerTestCategory {
	@Tolerate(min = 3, max = 3)
	private String category;

	public ConformerTestCategory(
		String category) {
		this.category = category;
	}

	public ConformerTestCategory(ConformerTestCategory t) {
		this.category = t.category;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}
