package ie.bitstep.mango.collections;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class MapBuilderTest {
	@Test
	void testMap() {
		Map<String, String> map = MapBuilder.<String, String>map().build();

		assertThat(map).isInstanceOf(LinkedHashMap.class).isEmpty();
	}

	@Test
	void testMapWithImplementation() {
		Map<String, String> map = MapBuilder.<String, String>map(new TreeMap<>()).build();

		assertThat(map).isInstanceOf(TreeMap.class).isEmpty();
	}

	@Test
	void testWith() {
		Map<String, String> m = MapBuilder.<String, String>map()
			.with("name", "Darthvader")
			.build();

		assertThat(m).hasSize(1).containsEntry("name", "Darthvader");
	}

	@Test
	void testWithNestedMap() {
		Map m = MapBuilder.<String, Object>map()
			.with("first", "Tom")
			.with("last", "Cruise")
			.with("address",
				MapBuilder.<String, String>map()
					.with("line1", "One South County")
					.with("line2", "Leopardstown")
					.build()
			).build();

		assertThat(m).hasSize(3)
			.containsEntry("first", "Tom")
			.containsEntry("last", "Cruise")
			.containsKey("address");

		Map address = ((Map) m.get("address"));
		assertThat(address).hasSize(2)
			.containsEntry("line1", "One South County")
			.containsEntry("line2", "Leopardstown");
	}

	@Test
	void testWithOverlay() {
		Map address = MapBuilder.<String, Object>map()
			.with("address",
				MapBuilder.<String, String>map()
					.with("line1", "Two South County")
					.with("line2", "Leopardstown")
					.build())
			.build();

		Map m = MapBuilder.<String, Object>map()
			.with("first", "Tom")
			.with("last", "Cruise")
			.injectAll(address)
			.build();

		assertThat(m).hasSize(3)
			.containsEntry("first", "Tom")
			.containsEntry("last", "Cruise")
			.containsKey("address");


		Map retrievedAddress = ((Map) m.get("address"));
		assertThat(retrievedAddress).hasSize(2)
			.containsEntry("line1", "Two South County")
			.containsEntry("line2", "Leopardstown");
	}

	@Test
	void testWithMissing() {
		Map address = MapBuilder.<String, Object>map()
			.with("address",
				MapBuilder.<String, String>map()
					.with("line1", "Two South County")
					.with("line2", "Leopardstown")
					.build())
			.build();

		Map m = MapBuilder.<String, Object>map()
			.with("first", "Tom")
			.with("last", "Cruise")
			.injectMissing(address)
			.build();

		assertThat(m).hasSize(3)
			.containsEntry("first", "Tom")
			.containsEntry("last", "Cruise")
			.containsKey("address");

		Map retrievedAddress = ((Map) m.get("address"));
		assertThat(retrievedAddress).hasSize(2)
			.containsEntry("line1", "Two South County")
			.containsEntry("line2", "Leopardstown");
	}

	@Test
	void testWithMissingAlreadyExists() {
		Map address1 = MapBuilder.<String, Object>map()
			.with("address",
				MapBuilder.<String, String>map()
					.with("line1", "One South County")
					.with("line2", "Leopardstown")
					.build())
			.build();

		Map address2 = MapBuilder.<String, Object>map()
			.with("address",
				MapBuilder.<String, String>map()
					.with("line1", "Two South County")
					.with("line2", "Leopardstown")
					.build())
			.build();

		Map m = MapBuilder.<String, Object>map()
			.with("first", "Tom")
			.with("last", "Cruise")
			.injectAll(address1)
			.injectMissing(address2)
			.build();

		assertThat(m).hasSize(3)
			.containsEntry("first", "Tom")
			.containsEntry("last", "Cruise")
			.containsKey("address");

		Map retrievedAddress = ((Map) m.get("address"));
		assertThat(retrievedAddress).hasSize(2)
			.containsEntry("line1", "One South County")
			.containsEntry("line2", "Leopardstown");
	}

	@Test
	void testWithUpdate() {
		Map address1 = MapBuilder.<String, Object>map()
			.with("address",
				MapBuilder.<String, String>map()
					.with("line1", "One South County")
					.with("line2", "Leopardstown")
					.build())
			.build();

		Map address2 = MapBuilder.<String, Object>map()
			.with("address",
				MapBuilder.<String, String>map()
					.with("line1", "Two South County")
					.with("line2", "Leopardstown")
					.build())
			.build();

		Map m = MapBuilder.<String, Object>map()
			.with("first", "Tom")
			.with("last", "Cruise")
			.injectAll(address1)
			.updateExisting(address2)
			.build();

		assertThat(m).hasSize(3)
			.containsEntry("first", "Tom")
			.containsEntry("last", "Cruise")
			.containsKey("address");

		Map retrievedAddress = ((Map) m.get("address"));
		assertThat(m).containsKey("address");
		assertThat(retrievedAddress).hasSize(2)
			.containsEntry("line1", "Two South County")
			.containsEntry("line2", "Leopardstown");
	}

	@Test
	void testWithUpdateNoEntry() {
		Map address = MapBuilder.<String, Object>map()
			.with("address",
				MapBuilder.<String, String>map()
					.with("line1", "Two South County")
					.with("line2", "Leopardstown")
					.build())
			.build();

		Map m = MapBuilder.<String, Object>map()
			.with("first", "Tom")
			.with("last", "Cruise")
			.updateExisting(address)
			.build();

		assertThat(m).hasSize(2)
			.containsEntry("first", "Tom")
			.containsEntry("last", "Cruise")
			.doesNotContainKey("address");
	}

	@Test
	void withPath() {
		MapBuilder<String, Object> mapBuilder = MapBuilder.<String, Object>map();

		mapBuilder
			.withPath("manifest", "services")
			.with("pg", "PostgreSQL")
			.with("redis", "Redis");

		mapBuilder
			.withPath("manifest", "services")
			.with("queue", "Axon");

		mapBuilder
			.withPath("manifest", "context")
			.with("platform", "MKS");

		Map map = mapBuilder.build();

		Map expected = new LinkedHashMap();
		Map expectedManifest = new LinkedHashMap<>();
		Map expectedServices = new LinkedHashMap<>();
		Map expectedContext = new LinkedHashMap<>();
		expectedManifest.put("services", expectedServices);
		expectedManifest.put("context", expectedContext);

		expectedServices.put("pg", "PostgreSQL");
		expectedServices.put("redis", "Redis");
		expectedServices.put("queue", "Axon");

		expectedContext.put("platform", "MKS");

		expected.put("manifest", expectedManifest);

		assertThat(map).isEqualTo(expected);
	}
}