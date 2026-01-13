package ie.bitstep.mango.crypto.hmac;

import ie.bitstep.mango.crypto.annotations.UniqueGroup;

import java.lang.reflect.Field;

record UniqueGroupMember(UniqueGroup uniqueGroup, Field field) implements Comparable<UniqueGroupMember> {


	@Override
	public int compareTo(UniqueGroupMember o) {
		return Integer.compare(this.uniqueGroup.order(), o.uniqueGroup.order());
	}
}