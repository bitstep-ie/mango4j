package ie.bitstep.mango.collections;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class CollectionReconcilerTest {

	private static final class User {
		private final String id;
		private final String name;

		private User(String id, String name) {
			this.id = id;
			this.name = name;
		}

		String getId() {
			return id;
		}

		@Override
		public String toString() {
			return "User{" +
					"id='" + id + '\'' +
					", name='" + name + '\'' +
					'}';
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof User user)) return false;
			return id.equals(user.id) && name.equals(user.name);
		}

		@Override
		public int hashCode() {
			int result = id.hashCode();
			result = 31 * result + name.hashCode();
			return result;
		}
	}

	@Test
	void calcInitialKeySetCapacity() {
		assertEquals(200, CollectionReconciler.calcInitialKeySetCapacity(100));
	}

	@Test
	void calcInitialDesiredSize() {
		Iterable<Integer> param = () -> Arrays.asList(1, 2).iterator();

		assertEquals(2, CollectionReconciler.calcInitialDesiredSize(List.of("Hello", "Goodbye")));
		assertEquals(16, CollectionReconciler.calcInitialDesiredSize(param));
	}

	@Test
	void reconcile_list_basicAddRemoveAndOrder() {
		User u1 = new User("1", "Alice");
		User u2 = new User("2", "Bob");
		List<User> current = new ArrayList<>(Arrays.asList(u1, u2));

// Note: different instance with same id "2"
		User u2Updated = new User("2", "Bob Updated");
		User u3 = new User("3", "Charlie");
		List<User> desired = Arrays.asList(u2Updated, u3);

		List<User> returned =
				CollectionReconciler.reconcile(current, desired, User::getId);

// Should be the same list instance
		assertSame(current, returned);

// Order should match desired's id order: 2, 3
		assertEquals(2, current.size());
		assertEquals("2", current.get(0).getId());
		assertEquals("3", current.get(1).getId());

// Instance for id "2" should be the original (u2), not the desired one
		assertSame(u2, current.get(0));
		assertSame(u3, current.get(1));
	}

	@Test
	void reconcileAndReport_list_resultContainsAddedRemovedKept() {
		User u1 = new User("1", "Alice");
		User u2 = new User("2", "Bob");
		List<User> current = new ArrayList<>(Arrays.asList(u1, u2));

		User u2Updated = new User("2", "Bob Updated");
		User u3 = new User("3", "Charlie");
		List<User> desired = Arrays.asList(u2Updated, u3);

		CollectionReconciler.Result<User> result =
				CollectionReconciler.reconcileAndReport(current, desired, User::getId);

// Current list mutated
		assertEquals(2, current.size());
		assertEquals("2", current.get(0).getId());
		assertEquals("3", current.get(1).getId());

// Kept: id "2" (original instance)
		assertEquals(1, result.getKept().size());
		assertSame(u2, result.getKept().get(0));

// Added: id "3"
		assertEquals(1, result.getAdded().size());
		assertSame(u3, result.getAdded().get(0));

// Removed: id "1"
		assertEquals(1, result.getRemoved().size());
		assertSame(u1, result.getRemoved().get(0));
	}

	@Test
	void reconcile_withLinkedHashSet_preservesDesiredOrder() {
		User u1 = new User("1", "Alice");
		User u2 = new User("2", "Bob");
		Set<User> current = new LinkedHashSet<>(Arrays.asList(u1, u2));

		User u3 = new User("3", "Charlie");
		User u4 = new User("4", "Dana");
		Set<User> desired = new LinkedHashSet<>(Arrays.asList(u4, u3));

		CollectionReconciler.reconcile(current, desired, User::getId);

// Set iteration order should follow desired: 4, 3
		List<User> asList = new ArrayList<>(current);
		assertEquals(2, asList.size());
		assertEquals("4", asList.get(0).getId());
		assertEquals("3", asList.get(1).getId());
	}

	@Test
	void reconcile_emptyDesiredClearsCurrent_andResultReflectsRemoval() {
		User u1 = new User("1", "Alice");
		User u2 = new User("2", "Bob");
		List<User> current = new ArrayList<>(Arrays.asList(u1, u2));

		List<User> desired = List.of();

		CollectionReconciler.Result<User> result =
				CollectionReconciler.reconcileAndReport(current, desired, User::getId);

		assertTrue(current.isEmpty(), "current should be cleared");
		assertTrue(result.getAdded().isEmpty(), "no added elements");
		assertEquals(2, result.getRemoved().size());
		assertTrue(result.getKept().isEmpty());
		assertTrue(result.getRemoved().contains(u1));
		assertTrue(result.getRemoved().contains(u2));
	}

	@Test
	void reconcile_duplicateKeysInCurrentThrows() {
		User u1 = new User("1", "Alice");
		User u1Duplicate = new User("1", "Alice Duplicate");

		List<User> current = new ArrayList<>(Arrays.asList(u1, u1Duplicate));
		List<User> desired = List.of(new User("1", "Alice Target"));

		IllegalArgumentException ex = assertThrows(
				IllegalArgumentException.class,
				() -> CollectionReconciler.reconcile(current, desired, User::getId)
		);
		assertTrue(ex.getMessage().contains("Duplicate key in current"));
	}

	@Test
	void reconcile_duplicateKeysInDesiredThrows() {
		User u1 = new User("1", "Alice");
		List<User> current = new ArrayList<>(List.of(u1));

		List<User> desired = Arrays.asList(
				new User("1", "Alice v1"),
				new User("1", "Alice v2")
		);

		IllegalArgumentException ex = assertThrows(
				IllegalArgumentException.class,
				() -> CollectionReconciler.reconcile(current, desired, User::getId)
		);
		assertTrue(ex.getMessage().contains("Duplicate key in desired"));
	}

	@Test
	void reconcile_nullKeyThrowsNpe() {
		User u1 = new User(null, "NoId");
		List<User> current = new ArrayList<>(List.of(u1));
		List<User> desired = List.of();

		assertThrows(
				NullPointerException.class,
				() -> CollectionReconciler.reconcile(current, desired, User::getId)
		);
	}

	@Test
	void reconcileAndReport_withEmptyCollectionsProducesEmptyResult() {
		List<User> current = new ArrayList<>();
		List<User> desired = new ArrayList<>();

		CollectionReconciler.Result<User> result =
				CollectionReconciler.reconcileAndReport(current, desired, User::getId);

		assertTrue(current.isEmpty(), "current should remain empty");
		assertTrue(result.getAdded().isEmpty());
		assertTrue(result.getRemoved().isEmpty());
		assertTrue(result.getKept().isEmpty());
	}

	@Test
	void resultToString_includesAddedRemovedKept() {
		User u1 = new User("1", "Alice");
		User u2 = new User("2", "Bob");
		User u3 = new User("3", "Charlie");
		List<User> current = new ArrayList<>(Arrays.asList(u1, u2));
		List<User> desired = Arrays.asList(u2, u3);
		CollectionReconciler.Result<User> result = CollectionReconciler.reconcileAndReport(current, desired, User::getId);
		String str = result.toString();
		assertTrue(str.contains("added"));
		assertTrue(str.contains("removed"));
		assertTrue(str.contains("kept"));
		assertTrue(str.contains("Alice"));
		assertTrue(str.contains("Bob"));
		assertTrue(str.contains("Charlie"));
	}

	@Test
	void calcInitialSize_returnsExpectedValues() {
		List<Integer> empty = new ArrayList<>();
		List<Integer> small = Arrays.asList(1, 2, 3);
		List<Integer> sixteen = new ArrayList<>();
		for (int i = 0; i < 8; i++) sixteen.add(i); // 8*2=16
		List<Integer> large = new ArrayList<>();
		for (int i = 0; i < 20; i++) large.add(i); // 20*2=40

		assertEquals(16, CollectionReconciler.calcInitialSize(empty));
		assertEquals(16, CollectionReconciler.calcInitialSize(small));
		assertEquals(16, CollectionReconciler.calcInitialSize(sixteen));
		assertEquals(40, CollectionReconciler.calcInitialSize(large));
	}
}