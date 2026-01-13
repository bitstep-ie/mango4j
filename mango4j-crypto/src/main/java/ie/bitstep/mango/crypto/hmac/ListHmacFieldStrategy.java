package ie.bitstep.mango.crypto.hmac;

import ie.bitstep.mango.crypto.HmacStrategyHelper;
import ie.bitstep.mango.crypto.annotations.Hmac;
import ie.bitstep.mango.crypto.annotations.UniqueGroup;
import ie.bitstep.mango.crypto.core.domain.CryptoKey;
import ie.bitstep.mango.crypto.core.domain.HmacHolder;
import ie.bitstep.mango.crypto.core.exceptions.NoHmacKeysFoundException;
import ie.bitstep.mango.crypto.core.exceptions.NonTransientCryptoException;
import ie.bitstep.mango.crypto.domain.CryptoShieldHmacHolder;
import ie.bitstep.mango.crypto.domain.Lookup;
import ie.bitstep.mango.crypto.domain.Unique;
import ie.bitstep.mango.crypto.exceptions.HmacTokenizerInstantiationException;
import ie.bitstep.mango.crypto.exceptions.InvalidUniqueGroupDefinition;
import ie.bitstep.mango.crypto.exceptions.NoHmacFieldsFoundException;
import ie.bitstep.mango.crypto.tokenizers.HmacTokenizer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ie.bitstep.mango.crypto.hmac.FieldValidator.validateSourceHmacField;
import static ie.bitstep.mango.crypto.utils.ReflectionUtils.getFieldStringValue;
import static ie.bitstep.mango.crypto.utils.ReflectionUtils.getFieldsByAnnotation;
import static java.util.Collections.emptySet;
import static java.util.Objects.nonNull;

/**
 * This is the most flexible and probably powerful strategy the library uses. However,
 * it might not be the most suitable for relational DBs. The reason for this is that it will generate a list of lookup
 * {@link HmacHolder hmacHolder} objects for fields marked with
 * {@link Hmac.Purposes#LOOKUP LOOKUP} and another separate list of unique values
 * {@link HmacHolder hmacHolder} objects for fields marked with
 * {@link Hmac.Purposes#UNIQUE UNIQUE}. This will probably mean
 * that the application has to store the lookup list in a separate table and the unique values in a unique values table
 * (with the correct unique constraint definitions). This could require from 1 to 3 inserts for each write operation
 * on a record and would involve the associated search queries needing an SQL JOIN (back to the actual entity).
 * This strategy is very suitable for document DBs such as MongoDB because these lists can be stored in the actual entity
 * which will have a negligible impact on performance (and probably makes for nicer looking search code).
 * <p>
 * This strategy is more powerful because the lists on an entity can support any number of HMAC values for each field
 * which means that tenants could have more than the standard maximum HMAC key list size of 2 (usually 1 but during
 * a key rotation 2), or we could generate multiple HMACs per field for search purposes (e.g. for a PAN we could
 * generate the original PAN, the PAN without dashes, the last 4 digits of the PAN...all for a single field).
 * This strategy can support passive HMAC key rotation since there could be a ton of keys on a tenant over time
 * (although that's not recommended!) plus it would allow updating to a new key (i.e. adding a third key)
 * in the middle of a key rotation (in case of a mistake with new key assignment) without any functional impact to
 * the application whatsoever.
 * </p>
 */
public class ListHmacFieldStrategy implements HmacStrategy {

	private final Map<Field, Hmac> entityHmacFields = new HashMap<>();
	private final UniqueGroupSet entityUniqueGroups = new UniqueGroupSet();
	private final Map<Field, Set<HmacTokenizer>> entityHmacTokenizers = new HashMap<>();
	private final HmacStrategyHelper hmacStrategyHelper;

	public ListHmacFieldStrategy(Class<?> annotatedEntityClass, HmacStrategyHelper hmacStrategyHelper) {
		this.hmacStrategyHelper = hmacStrategyHelper;
		this.register(annotatedEntityClass);
	}

	private void register(Class<?> annotatedEntityClass) {
		List<Field> hmacSourceFields = getFieldsByAnnotation(annotatedEntityClass, Hmac.class);
		validateEntity(annotatedEntityClass, hmacSourceFields);
		populateHmacFields(annotatedEntityClass, hmacSourceFields);
		populateNonHmacUniqueGroupFields(annotatedEntityClass);
		if (entityHmacFields.isEmpty() && entityUniqueGroups.isEmpty()) {
			throw new NoHmacFieldsFoundException(String.format("Class '%s' does not have any fields annotated with %s", annotatedEntityClass.getName(), Hmac.class.getSimpleName()));
		}
		validateUniqueGroupSet();
	}

	private void validateUniqueGroupSet() {
		entityUniqueGroups.getGroups().forEach((uniqueGroupName, uniqueGroup) -> {
			int count = 1;
			for (UniqueGroupMember uniqueGroupMember : uniqueGroup.getUniqueGroupWrappers()) {
				if (uniqueGroupMember.uniqueGroup().order() != count++) {
					throw new InvalidUniqueGroupDefinition(String.format("The fields in the unique group '%s' have invalid orderings", uniqueGroupMember.uniqueGroup().name()));
				}
			}
		});
	}

	private void validateEntity(Class<?> annotatedEntityClass, List<Field> hmacSourceFields) {
		validateLookupDefinition(annotatedEntityClass, hmacSourceFields);
		validateUniqueDefinition(annotatedEntityClass, hmacSourceFields);
	}

	private void validateUniqueDefinition(Class<?> annotatedEntityClass, List<Field> hmacSourceFields) {
		if (hmacSourceFields.stream()
				.map(field -> field.getAnnotation(Hmac.class))
				.flatMap(hmac -> Arrays.stream(hmac.purposes())).anyMatch(purposes -> purposes == Hmac.Purposes.UNIQUE)) {
			validateEntityClassUniqueDefinition(annotatedEntityClass);
		} else {
			validateEntityNotAssignableFromMissingPurpose(annotatedEntityClass, Hmac.Purposes.UNIQUE);
		}
	}

	private void validateLookupDefinition(Class<?> annotatedEntityClass, List<Field> hmacSourceFields) {
		if (hmacSourceFields.stream()
				.map(field -> field.getAnnotation(Hmac.class))
				.flatMap(hmac -> Arrays.stream(hmac.purposes())).anyMatch(purposes -> purposes == Hmac.Purposes.LOOKUP)) {
			validateEntityClassLookupDefinition(annotatedEntityClass);
		} else {
			validateEntityNotAssignableFromMissingPurpose(annotatedEntityClass, Hmac.Purposes.LOOKUP);
		}
	}

	/**
	 * This is to stop applications from implementing one of the HMAC interfaces (e.g. {@link Lookup} or {@link Unique})
	 * but not having the corresponding {@link Hmac.Purposes purpose}
	 * (e.g. {@link Hmac.Purposes#LOOKUP LOOKUP} or
	 * {@link Hmac.Purposes#UNIQUE UNIQUE})
	 * on any of the {@link Hmac} fields. That would cause problems.
	 * @param annotatedEntityClass entity class being registered
	 * @param purpose {@link Hmac.Purposes purpose} which we check is
	 * on at least one {@link Hmac} field on the class.
	 */
	private void validateEntityNotAssignableFromMissingPurpose(Class<?> annotatedEntityClass, Hmac.Purposes purpose) {
		if (correspondingInterface(purpose).isAssignableFrom(annotatedEntityClass)) {
			throw new NonTransientCryptoException(String.format("%1$s implements %2$s but does not have any fields marked with @%3$s with %4$s purpose." +
							" Please either add a %3$s field with %4$s purpose or remove 'implements %2$s' from this class",
					annotatedEntityClass.getSimpleName(), correspondingInterface(purpose).getSimpleName(),
					Hmac.class.getSimpleName(), purpose));
		}
	}

	private static Class<?> correspondingInterface(Hmac.Purposes purpose) {
		return purpose == Hmac.Purposes.LOOKUP ? Lookup.class : Unique.class;
	}

	private void validateEntityClassUniqueDefinition(Class<?> annotatedEntityClass) {
		validateEntityClassDefinition(annotatedEntityClass, Hmac.Purposes.UNIQUE, Unique.class);
	}

	private void validateEntityClassLookupDefinition(Class<?> annotatedEntityClass) {
		validateEntityClassDefinition(annotatedEntityClass, Hmac.Purposes.LOOKUP, Lookup.class);
	}

	private void validateEntityClassDefinition(Class<?> annotatedEntityClass, Hmac.Purposes purpose,
											   Class<?> listAwareClass) {
		if (!listAwareClass.isAssignableFrom(annotatedEntityClass)) {
			throw new NonTransientCryptoException(String.format("%1$s has at least one field marked with @%2$s with %3$s " +
							"containing %4$s but this class does not implement %5$s. " +
							"Please make %1$s implements the %5$s interface if you want this entity class to use the %6$s strategy",
					annotatedEntityClass.getSimpleName(), Hmac.class.getSimpleName(),
					Hmac.Purposes.class.getSimpleName(), purpose, listAwareClass.getSimpleName(),
					this.getClass().getSimpleName()));
		}
	}

	private void populateHmacFields(Class<?> annotatedEntityClass, List<Field> hmacSourceFields) {
		for (Field hmacSourceField : hmacSourceFields) {
			validateSourceHmacField(hmacSourceField, annotatedEntityClass);
			if (hmacSourceField.isAnnotationPresent(UniqueGroup.class)) {
				entityUniqueGroups.add(hmacSourceField);
				if (Arrays.stream(hmacSourceField.getAnnotation(Hmac.class).purposes()).anyMatch(purposes -> purposes == Hmac.Purposes.LOOKUP)) {
					// once a unique HMAC belongs to a UniqueGroup it only has a standalone HMAC calculation if it's
					// also for lookup purposes. Otherwise, it's either part of a group or it's not.
					entityHmacFields.put(hmacSourceField, hmacSourceField.getAnnotation(Hmac.class));
				}
			} else {
				entityHmacFields.put(hmacSourceField, hmacSourceField.getAnnotation(Hmac.class));
			}

			Class<? extends HmacTokenizer>[] hmacTokenizerClasses = hmacSourceField.getAnnotation(Hmac.class).hmacTokenizers();
			for (Class<? extends HmacTokenizer> hmacTokenizerClass : hmacTokenizerClasses) {
				try {
					entityHmacTokenizers.computeIfAbsent(hmacSourceField, field -> new HashSet<>())
							.add(hmacTokenizerClass.getDeclaredConstructor().newInstance());
				} catch (Exception e) {
					throw new HmacTokenizerInstantiationException(hmacTokenizerClass);
				}
			}
		}
	}

	private void populateNonHmacUniqueGroupFields(Class<?> annotatedEntityClass) {
		Set<Field> nonHmacUniqueGroupFields = getFieldsByAnnotation(annotatedEntityClass, UniqueGroup.class).stream()
				.filter(field -> !field.isAnnotationPresent(Hmac.class))
				.collect(Collectors.toSet());
		if (!nonHmacUniqueGroupFields.isEmpty()) {
			if (!entityUniqueGroups.getGroups().keySet().containsAll(nonHmacUniqueGroupFields.stream()
					.map(field -> field.getAnnotation(UniqueGroup.class).name())
					.collect(Collectors.toSet()))) {
				throw new InvalidUniqueGroupDefinition(String.format("There are fields marked with %s which only have plain text fields in the group " +
								"but no corresponding HMAC field as part of the group. " +
								"Each Unique Group must contain at least one field marked with %s",
						UniqueGroup.class.getSimpleName(), Hmac.class.getSimpleName()));
			}
			entityUniqueGroups.addAll(nonHmacUniqueGroupFields);
		}
	}

	@Override
	public void hmac(Object entity) {
		this.hmac(entity, null);
	}

	void hmac(Object entity, ListHmacFieldStrategyDelegate listHmacFieldStrategyDelegate) {
		Set<HmacHolder> allHmacHolders = new HashSet<>();
		List<HmacHolder> lookupHmacs = new ArrayList<>();
		List<HmacHolder> uniqueHmacs = new ArrayList<>();
		populateHmacHolders(entity, listHmacFieldStrategyDelegate, uniqueHmacs, lookupHmacs);

		if (!lookupHmacs.isEmpty() || !uniqueHmacs.isEmpty()) {
			if (listHmacFieldStrategyDelegate != null) {
				listHmacFieldStrategyDelegate.preProcessForRekey(entity, lookupHmacs, uniqueHmacs);
			}
			allHmacHolders.addAll(lookupHmacs);
			allHmacHolders.addAll(uniqueHmacs);

			hmacStrategyHelper.encryptionService()
					.hmac(allHmacHolders.stream()
							// skip hmac calculation for null values (HMAC of null is just null)
							.filter(hmacHolder -> nonNull(hmacHolder.getValue()))
							.toList());
			postProcess(entity, lookupHmacs, uniqueHmacs);
		}
	}

	private void populateHmacHolders(Object entity, ListHmacFieldStrategyDelegate listHmacFieldStrategyDelegate,
									 List<HmacHolder> uniqueHmacs, List<HmacHolder> lookupHmacs) {

		List<CryptoKey> currentHmacKeys = listHmacFieldStrategyDelegate != null
				? listHmacFieldStrategyDelegate.getCurrentHmacKeys()
				: getCurrentHmacKeys();
		validateHmacKeys(currentHmacKeys);
		for (Map.Entry<Field, Hmac> entry : entityHmacFields.entrySet()) {
			Field sourceField = entry.getKey();
			List<Hmac.Purposes> hmacPurposesForThisField = Arrays.asList(entry.getValue().purposes());
			String fieldValue = getFieldStringValue(entity, sourceField);
			Collection<HmacHolder> defaultHmacHoldersForThisField = listHmacFieldStrategyDelegate != null
					? listHmacFieldStrategyDelegate.getDefaultHmacHolders(currentHmacKeys, sourceField, fieldValue, entity)
					: this.getDefaultHmacHolders(currentHmacKeys, sourceField, fieldValue);

			if (hmacPurposesForThisField.contains(Hmac.Purposes.LOOKUP)) {
				Collection<HmacHolder> tokenizedHmacHolders = buildTokenizedHmacHolders(defaultHmacHoldersForThisField, sourceField);
				lookupHmacs.addAll(defaultHmacHoldersForThisField);
				lookupHmacs.addAll(tokenizedHmacHolders);
			}

			if (hmacPurposesForThisField.contains(Hmac.Purposes.UNIQUE) && !belongsToGroup(sourceField)) { // groups were already processed above
				if (!(entry.getValue().isOptionalUnique()) || fieldValue != null) {
					// disregard null values which are for optional standalone unique HMACs
					uniqueHmacs.addAll(defaultHmacHoldersForThisField);
				}
			}
		}
		populateCompoundUniqueHmacs(entity, uniqueHmacs);
	}

	private static void validateHmacKeys(List<CryptoKey> currentHmacKeys) {
		if (currentHmacKeys == null || currentHmacKeys.isEmpty()) {
			throw new NoHmacKeysFoundException();
		}
	}

	private void populateCompoundUniqueHmacs(Object entity, List<HmacHolder> uniqueHmacs) {
		UniqueGroupSet entityUniqueGroups = new UniqueGroupSet(this.entityUniqueGroups);
		entityUniqueGroups.getGroups().forEach((groupName, group) -> {
			for (UniqueGroupMember uniqueGroupMember : group.getUniqueGroupWrappers()) {
				String fieldValue = getFieldStringValue(entity, uniqueGroupMember.field());
				if (uniqueGroupMember.uniqueGroup().isOptional() && fieldValue == null) {
					// don't calculate compound unique constraint if this optional field has no value
					return;
				}
			}
			StringBuilder valueToHmac = new StringBuilder();
			group.getUniqueGroupWrappers().forEach(uniqueGroupMember -> valueToHmac.append(getFieldStringValue(entity, uniqueGroupMember.field())));
			List<HmacHolder> defaultCompoundUniqueHmacHolders = getCurrentHmacKeys().stream()
					.map(cryptoKey -> new HmacHolder(cryptoKey, valueToHmac.toString(), groupName))
					.toList();
			uniqueHmacs.addAll(defaultCompoundUniqueHmacHolders);
		});
	}

	private boolean belongsToGroup(Field sourceField) {
		return entityUniqueGroups.contains(sourceField);
	}

	private static void postProcess(Object entity, List<HmacHolder> newLookupHmacs, List<HmacHolder> newUniqueValueHmacs) {
		if (Lookup.class.isAssignableFrom(entity.getClass())) {
			retainExistingHmacs(((Lookup) entity)::getLookups, ((Lookup) entity)::setLookups, newLookupHmacs);
		}

		if (Unique.class.isAssignableFrom(entity.getClass())) {
			retainExistingHmacs(((Unique) entity)::getUniqueValues, ((Unique) entity)::setUniqueValues, newUniqueValueHmacs);
		}
	}

	private static void retainExistingHmacs(Supplier<Collection<CryptoShieldHmacHolder>> existingEntityHmacsSupplier,
											Consumer<Collection<CryptoShieldHmacHolder>> hmacsConsumer,
											List<HmacHolder> newHmacHolders) {

		Collection<CryptoShieldHmacHolder> existingEntityHmacHolders = existingEntityHmacsSupplier.get();
		if (existingEntityHmacHolders == null) {
			existingEntityHmacHolders = new ArrayList<>();
		}
		Collection<CryptoShieldHmacHolder> newEntityHmacs = new ArrayList<>(newHmacHolders.stream()
				.map(newHmacHolder -> new CryptoShieldHmacHolder(newHmacHolder.getCryptoKey().getId(), newHmacHolder.getValue(), newHmacHolder.getHmacAlias(), newHmacHolder.getTokenizedRepresentation()))
				.toList());
		for (CryptoShieldHmacHolder existingEntityHmac : existingEntityHmacHolders) {
			if (newEntityHmacs.stream().noneMatch(newEntityHmac -> newEntityHmac.getCryptoKeyId().equals(existingEntityHmac.getCryptoKeyId()))) {
				newEntityHmacs.add(existingEntityHmac);
			}
		}
		if (!newHmacHolders.isEmpty()) {
			hmacsConsumer.accept(newEntityHmacs);
		}
	}

	private Collection<HmacHolder> buildTokenizedHmacHolders(Collection<HmacHolder> defaultHmacHoldersForThisField, Field sourceField) {
		List<HmacHolder> tokenizedHmacHolders;
		tokenizedHmacHolders = defaultHmacHoldersForThisField.stream().
				flatMap(hmacHolder -> entityHmacTokenizers.getOrDefault(sourceField, emptySet()).stream()
						.flatMap(hmacTokenizer -> hmacTokenizer.generateTokenizedValues(hmacHolder).stream()))
				.toList();
		return tokenizedHmacHolders;
	}

	Collection<HmacHolder> getDefaultHmacHolders(List<CryptoKey> currentHmacKeys, Field sourceField, String fieldValue) {
		return currentHmacKeys.stream()
				.map(cryptoKey -> new HmacHolder(cryptoKey, fieldValue, sourceField.getName()))
				.toList();
	}

	List<CryptoKey> getCurrentHmacKeys() {
		return hmacStrategyHelper.cryptoKeyProvider().getCurrentHmacKeys();
	}
}
