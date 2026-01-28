package ie.bitstep.mango.utils.masking;

/**
 * Created by e048222 on 5/19/2017.
 */
public class MaskerMaker {
	/**
	 * Instantiates a masker using its no-args constructor.
	 *
	 * @param maskerClass the masker type
	 * @return the masker instance
	 */
	public Masker make(Class<? extends Masker> maskerClass) {
		try {
			return maskerClass.getConstructor().newInstance();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);    // must be able to instantiate
		}
	}

	/**
	 * Instantiates a masker based on a {@link Mask} annotation.
	 *
	 * @param mask the mask annotation
	 * @return the masker instance
	 */
	public Masker make(Mask mask) {
		try {
			return mask.masker().getConstructor(Mask.class).newInstance(mask);
		} catch (Exception ex1) {
			try {
				// Not a parameterised masker, return non-parameterised instance
				return make(mask.masker());
			}
			catch (Exception ex2) {
				throw new IllegalStateException(ex2);    // must be able to instantiate
			}
		}
	}

	/**
	 * Instantiates a masker using a masking character constructor, or falls back to no-args.
	 *
	 * @param maskerClass the masker type
	 * @param maskingCharacters the custom masking characters
	 * @return the masker instance
	 */
	public Masker make(Class<? extends Masker> maskerClass, String maskingCharacters) {
		Masker masker;

		try {
			masker = maskerClass.getConstructor(String.class).newInstance(maskingCharacters);
		} catch (NoSuchMethodException e) {
			// just default to no-args constructor
			masker = make(maskerClass);
		} catch (Exception ex) {
			throw new IllegalStateException(ex);    // must be able to instantiate
		}

		return masker;
	}
}
