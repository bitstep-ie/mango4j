package ie.bitstep.mango.utils.mutator.classes;

import ie.bitstep.mango.utils.conformance.Reduce;

import java.util.Objects;

public class Derived extends Base {
	@Reduce(max = 20, ellipsis = false)
	private String name;

	@Reduce(max = 6, ellipsis = false)
	private String label;

	public Derived(String errorCode, String error, String name, String label) {
		super(errorCode, error);
		this.name = name;
		this.label = label;
	}

	@Override
	public String toString() {
		return "Derived{" +
			"name='" + name + '\'' +
			", label='" + label + '\'' +
			"} " +
			super.toString();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (!(object instanceof Derived)) return false;
		if (!super.equals(object)) return false;
		Derived derived = (Derived) object;
		return Objects.equals(getName(), derived.getName()) && Objects.equals(getLabel(), derived.getLabel());
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getName(), getLabel());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
