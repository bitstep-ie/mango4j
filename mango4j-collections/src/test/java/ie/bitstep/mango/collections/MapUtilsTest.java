package ie.bitstep.mango.collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapUtilsTest {
	public static final String FIRST = "first";
	public static final String LAST = "last";
	public static final String TOM = "Tom";
	public static final String CRUISE = "Cruise";
	public static final String ADDRESS = "address";
	public static final String LINE = "line";
	public static final String LINE_1 = "1";
	public static final String ONE_SOUTH_COUNTY = "One South County";
	ObjectMapper om = new ObjectMapper();

	@Test
	void testEntriesToList() {
		Map<String, String> input = MapBuilder.<String, String>map().with("name", "java").build();
		Map<String, List<String>> output = MapUtils.<String, String>entriesToList(input);

		assertThat(output).hasSize(input.size());
		assertThat(output.get("name")).isInstanceOf(List.class);
		assertThat(output.get("name")).containsExactly("java");
	}

	@Test
	void testMapCombineNonEmptyTarget() throws IOException {
		Map<Object, Object> source = om.readValue(Paths.get("src/test/resources/source.json").toFile(), Map.class);
		Map<Object, Object> target = om.readValue(Paths.get("src/test/resources/target.json").toFile(), Map.class);
		Map<Object, Object> result = MapUtils.<Object, Object>merge(source, target);

		assertThat(result).containsKey("services");
		assertThat(MapUtils.getPath(result, "services")).containsKey("pg");
		assertThat(MapUtils.getPath(result, "services", "pg")).containsEntry("plan", "small");
		assertThat(MapUtils.getPath(result, "services", "pg")).containsKey("params");
		assertThat(MapUtils.getPath(result, "services", "pg", "params")).containsKey("memory");
		assertThat(MapUtils.getPath(result, "services", "pg", "params")).containsEntry("memory", true);
	}

	@Test
	void testMapCombineEmptyTarget() throws IOException {
		Map<Object, Object> source = om.readValue(Paths.get("src/test/resources/source.json").toFile(), Map.class);
		Map<Object, Object> target = new LinkedHashMap<>();
		Map<Object, Object> result = MapUtils.<Object, Object>merge(source, target);

		assertThat(result).containsKey("services");
		assertThat(MapUtils.getPath(result, "services")).containsKey("pg");
		assertThat(MapUtils.getPath(result, "services", "pg")).containsEntry("plan", "small");
		assertThat(MapUtils.getPath(result, "services", "pg")).containsKey("params");
		assertThat(MapUtils.getPath(result, "services", "pg", "params")).containsKey("memory");
		assertThat(MapUtils.getPath(result, "services", "pg", "params")).containsEntry("memory", true);
	}

	@Test
	void testMapCombineNullTarget() throws IOException {
		Map<Object, Object> source = om.readValue(Paths.get("src/test/resources/source.json").toFile(), Map.class);
		Map<Object, Object> result = MapUtils.<Object, Object>merge(source, null);

		assertThat(result).containsKey("services");
		assertThat(MapUtils.getPath(result, "services")).containsKey("pg");
		assertThat(MapUtils.getPath(result, "services", "pg")).containsEntry("plan", "small");
		assertThat(MapUtils.getPath(result, "services", "pg")).containsKey("params");
		assertThat(MapUtils.getPath(result, "services", "pg", "params")).containsKey("memory");
		assertThat(MapUtils.getPath(result, "services", "pg", "params")).containsEntry("memory", true);
	}

	@Test
	void testMapReplace() throws IOException {
		Map<Object, Object> source = om.readValue(Paths.get("src/test/resources/source.json").toFile(), Map.class);
		Map<Object, Object> target = om.readValue(Paths.get("src/test/resources/target.json").toFile(), Map.class);
		Map<Object, Object> result = MapUtils.<Object, Object>replace(source, target);

		assertThat(result).containsKey("services");
		assertThat(MapUtils.getPath(result, "services")).containsKey("pg");
		assertThat(MapUtils.getPath(result, "services", "pg")).containsEntry("plan", "small");
		assertThat(MapUtils.getPath(result, "services", "pg")).doesNotContainKey("params");
	}

	@Test
	void testMapCopy() throws IOException {
		Map<Object, Object> source = om.readValue(Paths.get("src/test/resources/source.json").toFile(), Map.class);
		Map<Object, Object> result = MapUtils.<Object, Object>copy(source);

		assertThat(result)
			.isEqualTo(source)
			.isNotSameAs(source);
	}

	@Test
	void testPut() {
		Map<String, Object> m = new LinkedHashMap<>();
		m = MapUtils.<String, Object>put(m, FIRST, TOM, LAST, CRUISE);

		assertThat(m)
			.containsEntry(FIRST, TOM)
			.containsEntry(LAST, CRUISE);
	}

	@Test
	void testPutException() {
		Map<String, Object> m = new LinkedHashMap<>();

		IllegalArgumentException throwUnbalancedKeyValuePairs = assertThrows(IllegalArgumentException.class, () ->
			MapUtils.<String, Object>put(m, FIRST, TOM, LAST)
		);

		assertThat(throwUnbalancedKeyValuePairs).isInstanceOf(IllegalArgumentException.class);

		IllegalArgumentException thrownNullMap = assertThrows(IllegalArgumentException.class, () ->
			MapUtils.<String, Object>put(null, FIRST, TOM, LAST, CRUISE)
		);

		assertThat(thrownNullMap).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testNode() {
		Map<String, Object> m = new LinkedHashMap<>();

		MapUtils.<String, Object>createPath(m, ADDRESS, LINE).put(LINE_1, ONE_SOUTH_COUNTY);

		assertThat(m.get(ADDRESS)).isInstanceOf(Map.class);
		assertThat(((Map) m.get(ADDRESS)).get(LINE)).isInstanceOf(Map.class);
		assertThat(m).containsKey(ADDRESS);
		assertThat((Map) ((Map) m.get(ADDRESS)).get(LINE)).containsKey(LINE_1);
	}

	@Test
	void testNodeExists() {
		Map<String, Object> m = new LinkedHashMap<>();

		MapUtils.<String, Object>createPath(m, ADDRESS, LINE); // create path
		MapUtils.<String, Object>createPath(m, ADDRESS, LINE).put(LINE_1, ONE_SOUTH_COUNTY); // set attribute

		assertThat(m.get(ADDRESS)).isInstanceOf(Map.class);
		assertThat(((Map) m.get(ADDRESS)).get(LINE)).isInstanceOf(Map.class);
		assertThat(m).containsKey(ADDRESS);
		assertThat((Map) ((Map) m.get(ADDRESS)).get(LINE)).containsKey(LINE_1);
	}

	@Test
	void testNodeExistsButNotMap() {
		Map<String, Object> m = new LinkedHashMap<>();

		MapUtils.<String, Object>createPath(m, ADDRESS).put(LINE, new LinkedList<>()); // create path

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
			MapUtils.<String, Object>createPath(m, ADDRESS, LINE)
		);

		assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
		assertThat(((Map) m.get(ADDRESS)).get(LINE)).isInstanceOf(List.class);
	}

	@Test
	void testNodeNullMap() {
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
			MapUtils.<String, Object>createPath(null, ADDRESS, LINE)
		);

		assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void getPath() {
		Map<String, Object> m = new LinkedHashMap<>();
		MapUtils.createPath(m, "services");

		assertThat(MapUtils.getPath(m, "services", "db")).isNull();
	}
}