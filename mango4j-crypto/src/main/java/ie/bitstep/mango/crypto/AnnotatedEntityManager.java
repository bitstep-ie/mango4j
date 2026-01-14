package ie.bitstep.mango.crypto;

import ie.bitstep.mango.crypto.annotations.CascadeEncrypt;
import ie.bitstep.mango.crypto.annotations.EnableMigrationSupport;
import ie.bitstep.mango.crypto.annotations.Encrypt;
import ie.bitstep.mango.crypto.annotations.EncryptedBlob;
import ie.bitstep.mango.crypto.annotations.EncryptionKeyId;
import ie.bitstep.mango.crypto.annotations.Hmac;
import ie.bitstep.mango.crypto.annotations.strategies.HmacStrategyToUse;
import ie.bitstep.mango.crypto.core.exceptions.NonTransientCryptoException;
import ie.bitstep.mango.crypto.hmac.DoubleHmacFieldStrategy;
import ie.bitstep.mango.crypto.hmac.HmacStrategy;
import ie.bitstep.mango.crypto.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.Collections.emptyList;

/**
 * Used to register an application's entities that contain encrypted fields.
 */
public class AnnotatedEntityManager {

	private static final System.Logger LOGGER = System.getLogger(AnnotatedEntityManager.class.getName());

	private final Map<Class<?>, List<Field>> sourceEncryptedFields = new HashMap<>();
	private final Map<Class<?>, List<Field>> allSourceConfidentialFields = new HashMap<>();
	private final Map<Class<?>, Field> applicationEncryptedDataFields = new HashMap<>();
	private final Map<Class<?>, Field> applicationEncryptedKeyIdFields = new HashMap<>();
	private final Map<Class<?>, HmacStrategy> applicationConfidentialEntitiesHmacStrategies = new HashMap<>();
	private final Map<Class<?>, List<Field>> sourceFieldsToCascadeEncrypt = new HashMap<>();

	/**
	 * Applications must call this method for all entity classes they have which contain the encryption annotations
	 * ({@link Encrypt @Encrypt} {@link Hmac @Hmac}, {@link EncryptedBlob @EncryptedBlob}). This method registers the
	 * default {@link DoubleHmacFieldStrategy} HMAC strategy for the entity type.
	 *
	 * @param annotatedEntityClasses Application's entity class.
	 * @param hmacStrategyHelper
	 */
	public AnnotatedEntityManager(Collection<Class<?>> annotatedEntityClasses, HmacStrategyHelper hmacStrategyHelper) {
		if (annotatedEntityClasses == null || annotatedEntityClasses.isEmpty() || annotatedEntityClasses.stream().anyMatch(Objects::isNull)) {
			throw new NullPointerException("Constructor parameters cannot be null");
		}
		annotatedEntityClasses.forEach(annotatedEntityClass -> {
			registerEncryptedDataField(annotatedEntityClass);
			registerFieldsToEncrypt(annotatedEntityClass);
			registerEncryptedKeyIdField(annotatedEntityClass);
			registerHmacStrategy(annotatedEntityClass, hmacStrategyHelper);
			registerAllConfidentialFields(annotatedEntityClass);
			registerFieldsToCascadeEncrypt(annotatedEntityClass);
		});
		validateCascadeEncryptFields();
	}

	private void validateCascadeEncryptFields() {
		sourceFieldsToCascadeEncrypt.values().stream().flatMap(Collection::stream).forEach(cascadedField -> {
			List<Field> encryptedFields = ReflectionUtils.getFieldsByAnnotation(cascadedField.getType(), Encrypt.class);
			List<Field> hmacFields = ReflectionUtils.getFieldsByAnnotation(cascadedField.getType(), Hmac.class);
			if (!(encryptedFields.isEmpty() && hmacFields.isEmpty()) && !allSourceConfidentialFields.containsKey(cascadedField.getType())) {
				throw new NonTransientCryptoException(String.format("Field '%s' was marked with @%s but " +
								"the field type is '%s' which wasn't registered. " +
								"You'll also need to register this type",
						cascadedField.getName(), CascadeEncrypt.class.getSimpleName(), cascadedField.getType()));
			} else if (encryptedFields.isEmpty() && hmacFields.isEmpty()
					&& (ReflectionUtils.getFieldsByAnnotation(cascadedField.getType(), CascadeEncrypt.class).isEmpty() && !Collection.class.isAssignableFrom(cascadedField.getType()))) {
				throw new NonTransientCryptoException(String.format("Field '%4$s' was marked with @%1$s but didn't have @%2$s or @%3$s fields and " +
								"also didn't have @%1$s fields either. Any fields marked with @%1$s must either be " +
								"encryptable objects or else contain further @%1$s fields", CascadeEncrypt.class.getSimpleName(),
						Encrypt.class.getSimpleName(), Hmac.class.getSimpleName(), cascadedField.getName()));
			}
		});
	}

	private void registerFieldsToCascadeEncrypt(Class<?> annotatedEntityClass) {
		List<Field> fieldsToCascadeEncrypt = ReflectionUtils.getFieldsByAnnotation(annotatedEntityClass, CascadeEncrypt.class);
		if (fieldsToCascadeEncrypt.isEmpty()) {
			return;
		}

		sourceFieldsToCascadeEncrypt.put(annotatedEntityClass, new ArrayList<>());
		fieldsToCascadeEncrypt.forEach(cascadedSourceField -> {
			if (cascadedSourceField.isAnnotationPresent(Encrypt.class)
					|| cascadedSourceField.isAnnotationPresent(Hmac.class)) {
				throw new NonTransientCryptoException(String.format("Fields marked with @%s cannot also be marked with @%s or @%s",
						CascadeEncrypt.class.getSimpleName(), Encrypt.class.getSimpleName(), Hmac.class.getSimpleName()));
			}
			cascadedSourceField.setAccessible(true);
			sourceFieldsToCascadeEncrypt.get(annotatedEntityClass).add(cascadedSourceField);
			registerFieldsToCascadeEncrypt(cascadedSourceField.getType());
		});
	}

	private void registerAllConfidentialFields(Class<?> annotatedEntityClass) {
		allSourceConfidentialFields.put(annotatedEntityClass, new ArrayList<>(sourceEncryptedFields.getOrDefault(annotatedEntityClass, new ArrayList<>())));
		ReflectionUtils.getFieldsByAnnotation(annotatedEntityClass, Hmac.class)
				.forEach(hmacSourceField -> {
					List<Field> sourceConfidentialFields = allSourceConfidentialFields.get(annotatedEntityClass);
					if (!sourceConfidentialFields.contains(hmacSourceField)) {
						sourceConfidentialFields.add(hmacSourceField);
					}
				});

		if (allSourceConfidentialFields.get(annotatedEntityClass).isEmpty()) {
			allSourceConfidentialFields.remove(annotatedEntityClass);
		}
	}

	private void registerHmacStrategy(Class<?> annotatedEntityClass, HmacStrategyHelper hmacStrategyHelper) {
		List<Field> hmacSourceFields = ReflectionUtils.getFieldsByAnnotation(annotatedEntityClass, Hmac.class);
		if (hmacSourceFields.isEmpty()) {
			return;
		}

		HmacStrategy hmacStrategy = createHmacStrategyInstance(annotatedEntityClass, hmacStrategyHelper);
		applicationConfidentialEntitiesHmacStrategies.putIfAbsent(annotatedEntityClass, hmacStrategy);
	}

	private HmacStrategy createHmacStrategyInstance(Class<?> annotatedEntityClass, HmacStrategyHelper hmacStrategyHelper) {
		HmacStrategyToUse hmacStrategyToUse = getHmacStrategyToUse(annotatedEntityClass);
		HmacStrategy hmacStrategyInstance;
		try {
			hmacStrategyInstance = hmacStrategyToUse.value().getDeclaredConstructor(Class.class, hmacStrategyHelper.getClass()).newInstance(annotatedEntityClass, hmacStrategyHelper);
		} catch (Exception e) {
			if (e instanceof InvocationTargetException && e.getCause() instanceof NonTransientCryptoException) {
				throw (NonTransientCryptoException) e.getCause();
			}
			throw new NonTransientCryptoException(String.format("Could not create an instance of %s class. Please make sure " +
					"it has a constructor which accepts an %s object", hmacStrategyToUse.value().getSimpleName(), hmacStrategyHelper.getClass().getSimpleName()), e);
		}
		return hmacStrategyInstance;
	}

	private HmacStrategyToUse getHmacStrategyToUse(Class<?> annotatedEntityClass) {
		Annotation[] annotations = annotatedEntityClass.getAnnotations();
		for (Annotation annotation : annotations) {
			if (annotation.annotationType().equals(HmacStrategyToUse.class)) {
				return (HmacStrategyToUse) annotation;
			}
			Annotation[] subAnnotations = annotation.annotationType().getAnnotations();
			for (Annotation subAnnotation : subAnnotations) {
				if (subAnnotation.annotationType().equals(HmacStrategyToUse.class)) {
					return (HmacStrategyToUse) subAnnotation;
				}
			}
		}

		throw new NonTransientCryptoException(String.format("No @%1$s annotation was found on class %2$s, even though there were " +
				"fields marked with the @%3$s Annotation. If you want to HMAC some fields make sure to add the @%1$s annotation to " +
				"the %2$s class.", HmacStrategyToUse.class.getSimpleName(), annotatedEntityClass.getSimpleName(), Hmac.class.getSimpleName()));
	}

	private void registerEncryptedDataField(Class<?> annotatedEntityClass) {
		List<Field> encryptedDataSourceFields = ReflectionUtils.getFieldsByAnnotation(annotatedEntityClass, Encrypt.class);
		if (encryptedDataSourceFields.isEmpty()) {
			return;
		}

		List<Field> encryptedDataField = ReflectionUtils.getFieldsByAnnotation(annotatedEntityClass, EncryptedBlob.class);
		if (encryptedDataField.size() != 1) {
			throw new NonTransientCryptoException(String.format("%s has a field marked with @%s but without a corresponding field marked with @%s",
					annotatedEntityClass.getSimpleName(), Encrypt.class.getSimpleName(), EncryptedBlob.class.getSimpleName()));
		}

		encryptedDataField.get(0).setAccessible(true); // NOSONAR
		applicationEncryptedDataFields.putIfAbsent(annotatedEntityClass, encryptedDataField.get(0));
	}

	private void registerFieldsToEncrypt(Class<?> annotatedEntityClass) {
		List<Field> fieldsToEncrypt = ReflectionUtils.getFieldsByAnnotation(annotatedEntityClass, Encrypt.class);
		fieldsToEncrypt.forEach(field -> {

			EnableMigrationSupport migrationSupport = field.getAnnotation(EnableMigrationSupport.class);

			if (migrationSupport != null) {
// Field has @EnableMigrationSupport, skip transient check and log warning/error
				handleMigrationSupport(annotatedEntityClass, field, migrationSupport);
			} else if (!Modifier.isTransient(field.getModifiers())) {
// Field doesn't have @EnableMigrationSupport and is not transient - throw exception
				throw new NonTransientCryptoException(String.format("%s has a field named %s marked with @%s but it is not transient. " +
								"Please mark any fields annotated with @%s as transient",
						annotatedEntityClass.getSimpleName(), field.getName(), Encrypt.class.getSimpleName(), Encrypt.class.getSimpleName()));
			}
			field.setAccessible(true); // NOSONAR
		});
		sourceEncryptedFields.putIfAbsent(annotatedEntityClass, fieldsToEncrypt);
	}

	private void handleMigrationSupport(Class<?> annotatedEntityClass, Field field, EnableMigrationSupport migrationSupport) {
		String completionByStr = migrationSupport.completedBy();
		LocalDate completionByDate;

		try {
			completionByDate = LocalDate.parse(completionByStr);
		} catch (DateTimeParseException e) {
			throw new NonTransientCryptoException(String.format(
					"Field %s.%s has @%s with invalid completedBy date format '%s'. Expected format: YYYY-MM-DD",
					annotatedEntityClass.getSimpleName(), field.getName(),
					EnableMigrationSupport.class.getSimpleName(), completionByStr), e);
		}

		LocalDate today = LocalDate.now();
		String ticket = migrationSupport.ticket().isEmpty() ? "N/A" : migrationSupport.ticket();
		String message = String.format(
				"Field %s.%s is marked with @%s. Justification: %s. Expected completion: %s. Ticket: %s",
				annotatedEntityClass.getSimpleName(), field.getName(),
				EnableMigrationSupport.class.getSimpleName(), migrationSupport.justification(),
				completionByStr, ticket);

		if (today.isAfter(completionByDate)) {
// After completion date - log ERROR
			LOGGER.log(ERROR, message + " - MIGRATION DEADLINE HAS PASSED!");
		} else {
// Before completion date - log WARNING
			LOGGER.log(WARNING, message);
		}
	}

	private void registerEncryptedKeyIdField(Class<?> annotatedEntityClass) {
		List<Field> encryptionKeyIdFields = ReflectionUtils.getFieldsByAnnotation(annotatedEntityClass, EncryptionKeyId.class);
		if (encryptionKeyIdFields.isEmpty()) {
			return;
		}

		if (encryptionKeyIdFields.size() > 1) {
			throw new NonTransientCryptoException(String.format("%s has more than 1 field marked with @%2$s. " +
							"Please only annotate a single field with @%2$s",
					annotatedEntityClass.getSimpleName(), EncryptionKeyId.class.getSimpleName()));
		}

		if (encryptionKeyIdFields.get(0).getType() != String.class) {
			throw new NonTransientCryptoException(String.format("%s has a field of type %s marked with %s. " +
							"Please change this field type to String",
					annotatedEntityClass.getSimpleName(), encryptionKeyIdFields.get(0).getType(), EncryptionKeyId.class.getSimpleName()));
		}
		encryptionKeyIdFields.get(0).setAccessible(true); // NOSONAR
		applicationEncryptedKeyIdFields.putIfAbsent(annotatedEntityClass, encryptionKeyIdFields.get(0));
	}

	public List<Field> getFieldsToEncrypt(Class<?> annotatedEntityClass) {
		return sourceEncryptedFields.getOrDefault(annotatedEntityClass, emptyList());
	}

	public List<Field> getAllConfidentialFields(Class<?> annotatedEntityClass) {
		return allSourceConfidentialFields.getOrDefault(annotatedEntityClass, emptyList());
	}

	public Field getEncryptedDataField(Class<?> annotatedEntityClass) {
		return applicationEncryptedDataFields.get(annotatedEntityClass);
	}

	public Optional<Field> getEncryptionKeyIdField(Class<?> annotatedEntityClass) {
		return Optional.ofNullable(applicationEncryptedKeyIdFields.get(annotatedEntityClass));
	}

	public Optional<HmacStrategy> getHmacStrategy(Class<?> annotatedEntityClass) {
		return Optional.ofNullable(applicationConfidentialEntitiesHmacStrategies.get(annotatedEntityClass));
	}

	public Collection<Field> getFieldsToCascadeEncrypt(Class<?> annotatedEntityClass) {
		return sourceFieldsToCascadeEncrypt.getOrDefault(annotatedEntityClass, emptyList());
	}
}