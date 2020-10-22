package com.lovetropics.minigames.common.minigames.statistics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.lovetropics.minigames.common.minigames.behaviours.instances.TeamsBehavior;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.lovetropics.minigames.common.minigames.statistics.StatisticDisplays.*;

public final class StatisticKey<T> {
	private static final Map<String, StatisticKey<?>> REGISTRY = new Object2ObjectOpenHashMap<>();

	// Generic - Per Player
	public static final StatisticKey<Integer> PLACEMENT = integer("placement").displays(placement());

	public static final StatisticKey<Integer> KILLS = integer("kills").displays(unit("kills"));
	public static final StatisticKey<Integer> POINTS = integer("points");

	public static final StatisticKey<Integer> TIME_SURVIVED = integer("time_survived").displays(minutesSeconds());
	public static final StatisticKey<String> CAUSE_OF_DEATH = string("cause_of_death");
	public static final StatisticKey<PlayerKey> KILLED_BY = player("killed_by");
	public static final StatisticKey<Integer> TIME_CAMPING = integer("time_camping").displays(minutesSeconds());
	public static final StatisticKey<TeamsBehavior.TeamKey> TEAM = team("team");

	public static final StatisticKey<Integer> BLOCKS_BROKEN = integer("blocks_broken").displays(unit("blocks"));

	// Generic - Global
	public static final StatisticKey<Integer> TOTAL_TIME = integer("total_time").displays(minutesSeconds());

	public static final StatisticKey<PlayerKey> WINNING_PLAYER = player("winning_player").displays(playerName());
	public static final StatisticKey<TeamsBehavior.TeamKey> WINNING_TEAM = team("winning_team");

	// Turtle Race
	public static final StatisticKey<Integer> PLAYER_COLLISIONS = integer("player_collisions").displays(unit("collisions"));

	// Trash Dive
	public static final StatisticKey<Integer> TRASH_COLLECTED = integer("trash_collected").displays(unit("trash"));

	// Signature Run
	public static final StatisticKey<Integer> SIGNATURES_COLLECTED = integer("signatures_collected").displays(unit("signatures"));

	// Conservation Exploration
	public static final StatisticKey<Integer> CREATURES_RECORDED = integer("creatures_recorded").displays(unit("creatures"));

	// Treasure Dig X
	public static final StatisticKey<Integer> CHESTS_OPENED = integer("chests_opened");
	public static final StatisticKey<Integer> EXPLOSIONS_CAUSED = integer("explosions_caused");

	private final String key;
	private final Function<T, JsonElement> serializer;
	private Function<T, String> display = simple();

	private StatisticKey(String key, Function<T, JsonElement> serializer) {
		this.key = key;
		this.serializer = serializer;
	}

	public static <T> StatisticKey<T> register(String key, Function<T, JsonElement> serializer) {
		StatisticKey<T> statistic = new StatisticKey<>(key, serializer);
		REGISTRY.put(key, statistic);
		return statistic;
	}

	public static StatisticKey<Integer> integer(String key) {
		return StatisticKey.register(key, JsonPrimitive::new);
	}

	public static StatisticKey<Boolean> bool(String key) {
		return StatisticKey.register(key, JsonPrimitive::new);
	}

	public static StatisticKey<String> string(String key) {
		return StatisticKey.register(key, JsonPrimitive::new);
	}

	public static StatisticKey<PlayerKey> player(String key) {
		return StatisticKey.register(key, PlayerKey::serializeId);
	}

	public static StatisticKey<TeamsBehavior.TeamKey> team(String key) {
		return StatisticKey.register(key, team -> new JsonPrimitive(team.key));
	}

	public static StatisticKey<IntList> intList(String key) {
		return StatisticKey.intoArray(key, (values, array) -> {
			IntListIterator iterator = values.iterator();
			while (iterator.hasNext()) {
				array.add(new JsonPrimitive(iterator.nextInt()));
			}
		});
	}

	public static StatisticKey<List<String>> stringList(String key) {
		return StatisticKey.list(key, JsonPrimitive::new);
	}

	public static <T> StatisticKey<List<T>> list(String key, Function<T, JsonElement> serializeElement) {
		return StatisticKey.intoArray(key, (values, array) -> {
			for (T value : values) {
				array.add(serializeElement.apply(value));
			}
		});
	}

	private static <T> StatisticKey<T> intoArray(String key, BiConsumer<T, JsonArray> serialize) {
		return StatisticKey.register(key, values -> {
			JsonArray array = new JsonArray();
			serialize.accept(values, array);
			return array;
		});
	}

	public StatisticKey<T> displays(Function<T, String> display) {
		this.display = display;
		return this;
	}

	public String getKey() {
		return key;
	}

	public JsonElement serialize(T value) {
		return serializer.apply(value);
	}

	@SuppressWarnings("unchecked")
	public JsonElement serializeUnchecked(Object value) {
		return serialize((T) value);
	}

	public String display(T value) {
		return display.apply(value);
	}

	@Nullable
	public static StatisticKey<?> get(String key) {
		return REGISTRY.get(key);
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}
}
