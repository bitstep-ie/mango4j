package ie.bitstep.mango.proxy;

import ie.bitstep.mango.utils.proxy.ProxyResolver;
import org.hibernate.proxy.HibernateProxy;

public class HibernateProxyResolver implements ProxyResolver {
	/**
	 * Resolves Hibernate proxies to their underlying implementation.
	 *
	 * @param o the object to resolve
	 * @return the unproxied object when applicable
	 */
	@Override
	public Object resolve(Object o) {
		if (isHibernateProxy(o)) {
			return ((HibernateProxy) o).getHibernateLazyInitializer().getImplementation();
		}

		return o;
	}

	/**
	 * Determines whether the supplied object is a Hibernate proxy.
	 *
	 * @param o the object to check
	 * @return true when the object is a proxy
	 */
	private static boolean isHibernateProxy(Object o) {
		return o instanceof HibernateProxy;
	}
}
