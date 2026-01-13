package ie.bitstep.mango.reflection.accessors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyGetter {
	String value();
}
