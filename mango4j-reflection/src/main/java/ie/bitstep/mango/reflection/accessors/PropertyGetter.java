package ie.bitstep.mango.reflection.accessors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyGetter {
	/**
	 * Specifies the field name this getter provides.
	 *
	 * @return the field name
	 */
	String value();
}
