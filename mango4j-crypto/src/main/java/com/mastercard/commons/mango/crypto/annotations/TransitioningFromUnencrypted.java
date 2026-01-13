package com.mastercard.commons.mango.crypto.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an existing entity field is in the process of being transitioned from unencrypted to encrypted.
 * Having this annotation on a field tells the library to bypass the transient validation checks and so allows the
 * field to not be marked as transient.
 * This is intended to be a temporary annotation to be used during a migration period and should be removed
 * once all entities of this type have been reencrypted/rekeyed (so that this value is now in the
 * {@link EncryptedBlob} field for all entities of this type), after which the field can be marked as transient and
 * have this annotation removed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface TransitioningFromUnencrypted {
}
