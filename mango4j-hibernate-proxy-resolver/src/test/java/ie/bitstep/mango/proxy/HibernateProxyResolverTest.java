package ie.bitstep.mango.proxy;

import ie.bitstep.mango.utils.proxy.ProxyResolver;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serial;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HibernateProxyResolverTest {
	@Test
	void testIsNotProxy() {
		ProxyResolver pr = new HibernateProxyResolver();

		assertThat(pr.resolve(this)).isEqualTo(this);
	}

	@Test
	void testIsProxy() {
		ProxyResolver pr = new HibernateProxyResolver();
		HibernateProxy hp = getHibernateProxy();
		Object resolved = pr.resolve(hp);

		assertThat(resolved).isNotNull().isNotEqualTo(hp);  // NOSONAR
	}

	private static HibernateProxy getHibernateProxy() {
		return new HibernateProxy() {
			@Serial
			@Override
			public Object writeReplace() {
				return null;
			}

			@Override
			public LazyInitializer getHibernateLazyInitializer() {
				return getLazyInitializer();
			}

			private static LazyInitializer getLazyInitializer() {
				return new LazyInitializer() {
					@Override
					public void initialize() throws HibernateException { // NOSONAR

					}

					@Override
					public Object getIdentifier() {
						return null;
					}

					@Override
					public void setIdentifier(Object o) { // NOSONAR

					}

					@Override
					public String getEntityName() {
						return "";
					}

					@Override
					public Class<?> getPersistentClass() {
						return null;
					}

					@Override
					public boolean isUninitialized() {
						return false;
					}

					@Override
					public Object getImplementation() {
						return new HibernateProxyResolverTest();
					}

					@Override
					public Object getImplementation(SharedSessionContractImplementor sharedSessionContractImplementor) throws HibernateException {
						return null;
					}

					@Override
					public void setImplementation(Object o) { // NOSONAR

					}

					@Override
					public Class<?> getImplementationClass() {
						return null;
					}

					@Override
					public String getImplementationEntityName() {
						return "";
					}

					@Override
					public boolean isReadOnlySettingAvailable() {
						return false;
					}

					@Override
					public boolean isReadOnly() {
						return false;
					}

					@Override
					public void setReadOnly(boolean b) { // NOSONAR

					}

					@Override
					public SharedSessionContractImplementor getSession() { // NOSONAR
						return null;
					}

					@Override
					public void setSession(SharedSessionContractImplementor sharedSessionContractImplementor) throws HibernateException {  // NOSONAR

					}

					@Override
					public void unsetSession() { // NOSONAR

					}

					@Override
					public void setUnwrap(boolean b) { // NOSONAR

					}

					@Override
					public boolean isUnwrap() {
						return false;
					}
				};
			}
		};
	}
}