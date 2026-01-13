package ie.bitstep.mango.utils.converters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.UnaryOperator;

/**
 * Mask annotation, generic definition to allow a custom masking definition
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Converter {

	/**
	 * Specify the converter
	 *
	 */
	Class<? extends UnaryOperator<Object>> converter() default NoConverter.class;
}
