package ie.bitstep.mango.utils.entity;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * ToStringBuilder to be used for hibernate entities.
 * <p>
 * The default ReflectionToStringBuilder calls toString on all child objects which forces Hibernate to load them all.
 * <p>
 * This class ignores all types except for
 * * primitives
 * * String
 * * Number
 * * Date
 */
public class EntityToStringBuilder extends ReflectionToStringBuilder {

	public EntityToStringBuilder(final Object object, final ToStringStyle style) {
		super(object, style);
	}

	public static String toString(final Object object) {
		return toString(object, null);
	}

	public static String toString(final Object object, final ToStringStyle style) {
		return new EntityToStringBuilder(object, style)
			.toString();
	}

	@Override
	protected boolean accept(Field f) {
		Class<?> fClass = f.getType();

		return super.accept(f) &&
			(fClass.isPrimitive() ||
				String.class.isAssignableFrom(fClass) ||
				Number.class.isAssignableFrom(fClass) ||
				Date.class.isAssignableFrom(fClass));
	}

}
