package ie.bitstep.mango.reflection.accessors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Accessor {
	/**
	 * Specifies the getter method name to use.
	 *
	 * @return the getter method name
	 */
	String getter() default "";

	/**
	 * Specifies the setter method name to use.
	 *
	 * @return the setter method name
	 */
	String setter() default "";
}
