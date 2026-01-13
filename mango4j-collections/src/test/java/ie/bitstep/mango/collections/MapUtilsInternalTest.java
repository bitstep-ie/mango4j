package ie.bitstep.mango.collections;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MapUtilsInternalTest {

	public static final String PROFILE = "profile";

	@Test
	void doListOverlayKeyExistsInjectMissingTrue() {
		Map source = MapBuilder.<String, Object>map()
			.with(PROFILE, new ArrayList<>())
			.build();

		Map target = MapBuilder.<String, Object>map()
			.with(PROFILE, new ArrayList<>())
			.build();

		MapUtilsInternal.doListOverlay(getEntry(source, PROFILE), target, true);

		assertThat(target).containsKey(PROFILE);
	}

	@Test
	void doListOverlayKeyExistsInjectMissingFalse() {
		Map source = MapBuilder.<String, Object>map()
			.with(PROFILE, new ArrayList<>())
			.build();

		Map target = MapBuilder.<String, Object>map()
			.with(PROFILE, new ArrayList<>())
			.build();

		MapUtilsInternal.doListOverlay(getEntry(source, PROFILE), target, false);

		assertThat(target).containsKey(PROFILE);
	}

	@Test
	void doListOverlayKeyDoesNotExistInjectMissingTrue() {
		Map source = MapBuilder.<String, Object>map()
			.with(PROFILE, new ArrayList<>())
			.build();

		Map target = MapBuilder.<String, Object>map()
			.build();

		MapUtilsInternal.doListOverlay(getEntry(source, PROFILE), target, true);

		assertThat(target).containsKey(PROFILE);
	}

	@Test
	void doListOverlayKeyDoesNotExistInjectMissingFalse() {
		Map source = MapBuilder.<String, Object>map()
			.with(PROFILE, new ArrayList<>())
			.build();

		Map target = MapBuilder.<String, Object>map()
			.build();

		MapUtilsInternal.doListOverlay(getEntry(source, PROFILE), target, false);

		assertThat(target).doesNotContainKey(PROFILE);
	}

	@Test
	void doMapOverlayKeyExistsInjectMissingTrue() {
		Map source = MapBuilder.<String, Object>map()
			.with(PROFILE, new LinkedHashMap<>())
			.build();

		Map target = MapBuilder.<String, Object>map()
			.with(PROFILE, new LinkedHashMap<>())
			.build();

		MapUtilsInternal.doMapOverlay(getEntry(source, PROFILE), target, true);

		assertThat(target).containsKey(PROFILE);
	}

	@Test
	void doMapOverlayKeyExistsInjectMissingFalse() {
		Map source = MapBuilder.<String, Object>map()
			.with(PROFILE, new LinkedHashMap<>())
			.build();

		Map target = MapBuilder.<String, Object>map()
			.with(PROFILE, new LinkedHashMap<>())
			.build();

		MapUtilsInternal.doMapOverlay(getEntry(source, PROFILE), target, false);

		assertThat(target).containsKey(PROFILE);
	}

	@Test
	void doMapOverlayKeyDoesNotExistInjectMissingTrue() {
		Map source = MapBuilder.<String, Object>map()
			.with(PROFILE, new LinkedHashMap<>())
			.build();

		Map target = MapBuilder.<String, Object>map()
			.build();

		MapUtilsInternal.doMapOverlay(getEntry(source, PROFILE), target, true);

		assertThat(target).containsKey(PROFILE);
	}

	@Test
	void doMapOverlayKeyDoesNotExistInjectMissingFalse() {
		Map source = MapBuilder.<String, Object>map()
			.with(PROFILE, new LinkedHashMap<>())
			.build();

		Map target = MapBuilder.<String, Object>map()
			.build();

		MapUtilsInternal.doMapOverlay(getEntry(source, PROFILE), target, false);

		assertThat(target).doesNotContainKey(PROFILE);
	}

	private Map.Entry<String, Object> getEntry(Map<String, Object> map, String key) {
		for (Map.Entry<String, Object> e: map.entrySet()) {
			if (e.getKey().equals(key)) {
				return e;
			}
		}

		return null;
	}

}