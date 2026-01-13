package ie.bitstep.mango.utils.proxy;

public class IdentityProxyResolver implements ProxyResolver {
	@Override
	public Object resolve(Object proxy) {
		return proxy;
	}
}
