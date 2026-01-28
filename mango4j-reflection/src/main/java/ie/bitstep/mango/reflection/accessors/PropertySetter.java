package ie.bitstep.mango.reflection.accessors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PropertySetter {
	/**
	 * Specifies the field name this setter updates.
	 *
	 * @return the field name
	 */
	String value();
}
