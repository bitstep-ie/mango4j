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

	/**
	 * Creates a builder for the supplied object.
	 *
	 * @param object the target object
	 * @param style the toString style
	 */
	public EntityToStringBuilder(final Object object, final ToStringStyle style) {
		super(object, style);
	}

	/**
	 * Builds a string representation using the default style.
	 *
	 * @param object the target object
	 * @return the string representation
	 */
	public static String toString(final Object object) {
		return toString(object, null);
	}

	/**
	 * Builds a string representation using the supplied style.
	 *
	 * @param object the target object
	 * @param style the toString style
	 * @return the string representation
	 */
	public static String toString(final Object object, final ToStringStyle style) {
		return new EntityToStringBuilder(object, style)
			.toString();
	}

	/**
	 * Accepts only safe-to-render field types for entity toString.
	 *
	 * @param f the field to consider
	 * @return true if the field should be included
	 */
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
