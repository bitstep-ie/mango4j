package ie.bitstep.mango.utils.proxy;

public interface ProxyResolver {
	/**
	 * Resolves a proxy object to its underlying implementation.
	 *
	 * @param proxy the proxy instance
	 * @return the resolved instance
	 */
	Object resolve(Object proxy);
}
