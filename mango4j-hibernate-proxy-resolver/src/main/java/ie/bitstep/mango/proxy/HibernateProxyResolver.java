package ie.bitstep.mango.proxy;

import ie.bitstep.mango.utils.proxy.ProxyResolver;
import org.hibernate.proxy.HibernateProxy;

public class HibernateProxyResolver implements ProxyResolver {
	@Override
	public Object resolve(Object o) {
		if (isHibernateProxy(o)) {
			return ((HibernateProxy) o).getHibernateLazyInitializer().getImplementation();
		}

		return o;
	}

	private static boolean isHibernateProxy(Object o) {
		return o instanceof HibernateProxy;
	}
}
