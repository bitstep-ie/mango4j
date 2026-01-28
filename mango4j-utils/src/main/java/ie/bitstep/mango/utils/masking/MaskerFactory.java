package ie.bitstep.mango.utils.masking;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class MaskerFactory {

	static class Tuple {

		Class<? extends Masker> clz;
		String name;

		/**
		 * Creates a tuple key for a masker and name.
		 *
		 * @param clz the masker class
		 * @param name the tuple name
		 */
		Tuple(Class<? extends Masker> clz, String name) {
			this.clz = clz;
			this.name = name;
		}

		/**
		 * Compares tuple values for equality.
		 *
		 * @param o the other object
		 * @return true when equal
		 */
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Tuple tuple = (Tuple) o;
			return clz.equals(tuple.clz) && name.equals(tuple.name);
		}

		/**
		 * Returns the hash code for this tuple.
		 *
		 * @return the hash code
		 */
		@Override
		public int hashCode() {
			return Objects.hash(clz, name);
		}
	}

	// This is only non-final for tests which need to set this to different values
	private static MaskerMaker maskerMaker = new MaskerMaker();

	/**
	 * Prevents instantiation.
	 */
	private MaskerFactory() {
		// NOSONAR
	}

	private static final Map<Class<? extends Masker>, Masker> DEFAULT_MASKERS = new ConcurrentHashMap<>();
	private static final Map<Tuple, Masker> PARAMETERISED_MASKERS = new ConcurrentHashMap<>();
	private static final Map<Tuple, Masker> MASKERS_WITH_CUSTOM_MASKING_CHARACTERS = new ConcurrentHashMap<>();

	/**
	 * Returns the default masker for a class.
	 *
	 * @param maskerClass the masker type
	 * @return the masker instance
	 */
	public static Masker getMasker(Class<? extends Masker> maskerClass) {
		return DEFAULT_MASKERS.computeIfAbsent(maskerClass, maskerClass2 -> maskerMaker.make(maskerClass2));
	}

	/**
	 * Returns a masker configured from a {@link Mask} annotation.
	 *
	 * @param mask the mask annotation
	 * @return the masker instance
	 */
	public static Masker getMasker(Mask mask) {
		Masker masker;
		String hashCode = String.valueOf(mask.hashCode());
		if (!PARAMETERISED_MASKERS.containsKey(new Tuple(mask.masker(), hashCode))) {
			masker = maskerMaker.make(mask);
			PARAMETERISED_MASKERS.put(new Tuple(mask.masker(), hashCode), masker);
		} else {
			masker = PARAMETERISED_MASKERS.get(new Tuple(mask.masker(), hashCode));
		}
		return masker;
	}

	/**
	 * Returns a masker with custom masking characters.
	 *
	 * @param maskerClass the masker type
	 * @param customMaskingCharacters the custom masking characters
	 * @return the masker instance
	 */
	public static Masker getMasker(Class<? extends Masker> maskerClass, String customMaskingCharacters) {
		if (StringUtils.isEmpty(customMaskingCharacters)) {
			// don't allow blank values or null as the custom masking character - just default to the normal masker
			return getMasker(maskerClass);
		}

		return getMaskerWithCustomMaskingCharacter(maskerClass, customMaskingCharacters);
	}

	/**
	 * Returns or creates a masker with a specific masking character set.
	 *
	 * @param maskerClass the masker type
	 * @param customMaskingCharacters the custom masking characters
	 * @return the masker instance
	 */
	private static Masker getMaskerWithCustomMaskingCharacter(Class<? extends Masker> maskerClass, String customMaskingCharacters) {
		Masker masker;
		if (!MASKERS_WITH_CUSTOM_MASKING_CHARACTERS.containsKey(new Tuple(maskerClass, customMaskingCharacters))) {
			masker = maskerMaker.make(maskerClass, customMaskingCharacters);
			MASKERS_WITH_CUSTOM_MASKING_CHARACTERS.put(new Tuple(maskerClass, customMaskingCharacters), masker);
		} else {
			masker = MASKERS_WITH_CUSTOM_MASKING_CHARACTERS.get(new Tuple(maskerClass, customMaskingCharacters));
		}
		return masker;
	}
}
