package ie.bitstep.mango.reflection.classes;

import java.util.Objects;

public class Base {
	private String errorCode;

	private String error;

	public Base(String errorCode, String error) {
		this.errorCode = errorCode;
		this.error = error;
	}

	@Override
	public String toString() {
		return "Base{" +
			"errorCode='" + errorCode + '\'' +
			", error='" + error + '\'' +
			'}';
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (!(object instanceof Base)) return false;
		Base base = (Base) object;
		return Objects.equals(getErrorCode(), base.getErrorCode()) && Objects.equals(error, base.error);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getErrorCode(), error);
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
}
