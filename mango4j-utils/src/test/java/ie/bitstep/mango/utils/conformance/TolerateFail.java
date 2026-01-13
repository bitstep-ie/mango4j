package ie.bitstep.mango.utils.conformance;

import java.net.URI;

class TolerateFail {
	@Tolerate(max = 40, min = 0)
	private URI uri = URI.create("http://test.com/");

	public TolerateFail(TolerateFail r) {
		this.uri = r.uri;
	}

	public TolerateFail() {
	}
}
