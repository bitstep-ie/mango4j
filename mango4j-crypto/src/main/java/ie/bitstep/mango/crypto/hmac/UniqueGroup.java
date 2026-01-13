package ie.bitstep.mango.crypto.hmac;

import ie.bitstep.mango.crypto.core.exceptions.NonTransientCryptoException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

class UniqueGroup {
	private final Set<UniqueGroupMember> uniqueGroupMembers = new TreeSet<>(Comparator.naturalOrder());
	private final List<Field> allUniqueGroupFields = new ArrayList<>();

	void add(Field field) {
		ie.bitstep.mango.crypto.annotations.UniqueGroup uniqueGroup = field.getAnnotation(ie.bitstep.mango.crypto.annotations.UniqueGroup.class);
		if (uniqueGroup == null) {
			throw new NonTransientCryptoException(
				String.format("Field '%s' has no associated %s annotation", field.getName(), ie.bitstep.mango.crypto.annotations.UniqueGroup.class.getSimpleName()));
		}
		allUniqueGroupFields.add(field);
		uniqueGroupMembers.add(new UniqueGroupMember(uniqueGroup, field));
	}

	public List<Field> getAllFields() {
		return allUniqueGroupFields;
	}

	/**
	 * @return an ordered set ordered on {@link ie.bitstep.mango.crypto.annotations.UniqueGroup#order()}
	 */
	public Set<UniqueGroupMember> getUniqueGroupWrappers() {
		return uniqueGroupMembers;
	}
}
