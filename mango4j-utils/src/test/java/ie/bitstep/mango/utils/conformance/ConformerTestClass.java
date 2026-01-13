package ie.bitstep.mango.utils.conformance;

public class ConformerTestClass {
	@Tolerate(max = 8, min = 4)
	private String serviceName;

	@Tolerate(min = 6, max = 10)
	private String plan;

	@Reduce(max = 8, ellipsis = false)
	private String tag;

	@Reduce(max = 40)
	private String description;

	@Reduce(max = 2)
	private String shortString;

	@Tolerate(min = 10, max = 100)
	private Integer amount;

	@Tolerate(min = 30, max = 300)
	private Integer timeout;

	private ConformerTestCategory category;

	public ConformerTestClass(
			String serviceName,
			String tag,
			String plan,
			String description,
			String shortString,
			Integer amount,
			Integer timeout,
			ConformerTestCategory category) {
		this.serviceName = serviceName;
		this.tag = tag;
		this.plan = plan;
		this.description = description;
		this.shortString = shortString;
		this.amount = amount;
		this.timeout = timeout;
		this.category = category;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getTag() {
		return tag;
	}

	public String getPlan() {
		return plan;
	}

	public String getDescription() {
		return description;
	}

	public String getShortString() {
		return shortString;
	}

	public Integer getAmount() {
		return amount;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public ConformerTestCategory getCategory() {
		return category;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setPlan(String plan) {
		this.plan = plan;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setShortString(String shortString) {
		this.shortString = shortString;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public void setCategory(ConformerTestCategory category) {
		this.category = category;
	}
}
