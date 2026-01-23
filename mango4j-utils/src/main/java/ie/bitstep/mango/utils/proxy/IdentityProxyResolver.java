package ie.bitstep.mango.utils.proxy;

public class IdentityProxyResolver implements ProxyResolver {
	/**
	 * Returns the supplied object unchanged.
	 *
	 * @param proxy the proxy instance
	 * @return the same instance
	 */
	@Override
	public Object resolve(Object proxy) {
		return proxy;
	}
}
