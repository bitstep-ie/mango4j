package ie.bitstep.mango.crypto.hmac;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class UniqueGroupSet {
	private final Set<Field> fields = new HashSet<>();
	private Map<String, UniqueGroup> groups = new HashMap<>();

	UniqueGroupSet(UniqueGroupSet entityUniqueGroups) {
		this.groups = new HashMap<>(entityUniqueGroups.getGroups());
		this.fields.addAll(groups.values().stream().flatMap(uniqueGroup -> uniqueGroup.getAllFields().stream()).collect(Collectors.toSet()));
	}

	UniqueGroupSet() {
	}

	boolean contains(Field field) {
		return fields.contains(field);
	}

	void add(Field field) {
		ie.bitstep.mango.crypto.annotations.UniqueGroup[] uniqueUniqueGroups = getUniqueGroup(field);
		for (ie.bitstep.mango.crypto.annotations.UniqueGroup uniqueGroup : uniqueUniqueGroups) {
			groups.computeIfAbsent(uniqueGroup.name(), key -> new UniqueGroup()).add(field);
		}
		fields.add(field);
	}

	void addAll(Set<Field> uniqueGroupFields) {
		uniqueGroupFields.forEach(this::add);
	}

	private static ie.bitstep.mango.crypto.annotations.UniqueGroup[] getUniqueGroup(Field field) {
		return field.getAnnotationsByType(ie.bitstep.mango.crypto.annotations.UniqueGroup.class);
	}

	Map<String, UniqueGroup> getGroups() {
		return groups;
	}

	boolean isEmpty() {
		return groups.isEmpty();
	}
}
