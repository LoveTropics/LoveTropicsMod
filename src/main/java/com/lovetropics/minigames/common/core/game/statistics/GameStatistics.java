package com.lovetropics.minigames.common.core.game.statistics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;
import java.util.Set;

public final class GameStatistics {
	public static final JsonSerializer<GameStatistics> SERIALIZER = (statistics, type, ctx) -> statistics.serialize();

	private final StatisticsMap global = new StatisticsMap();
	private final Map<PlayerKey, StatisticsMap> byPlayer = new Object2ObjectOpenHashMap<>();

	public StatisticsMap getGlobal() {
		return global;
	}

	public StatisticsMap forPlayer(PlayerKey key) {
		return byPlayer.computeIfAbsent(key, p -> new StatisticsMap());
	}

	public StatisticsMap forPlayer(PlayerEntity player) {
		return forPlayer(PlayerKey.from(player));
	}

	public Set<PlayerKey> getPlayers() {
		return byPlayer.keySet();
	}

	public void clear(StatisticKey<?> key) {
		global.remove(key);
		for (StatisticsMap player : byPlayer.values()) {
			player.remove(key);
		}
	}

	public JsonElement serialize() {
		JsonObject root = new JsonObject();
		root.add("global", global.serialize());

		JsonArray playersArray = new JsonArray();
		for (Map.Entry<PlayerKey, StatisticsMap> entry : byPlayer.entrySet()) {
			PlayerKey player = entry.getKey();

			JsonObject playerRoot = new JsonObject();
			playerRoot.add("profile", player.serializeProfile());
			playerRoot.add("statistics", entry.getValue().serialize());

			playersArray.add(playerRoot);
		}

		root.add("players", playersArray);

		return root;
	}
}
