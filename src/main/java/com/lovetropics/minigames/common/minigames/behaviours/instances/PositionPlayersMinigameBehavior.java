package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.dimension.DimensionUtils;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class PositionPlayersMinigameBehavior implements IMinigameBehavior {
	private final String[] participantSpawnKeys;
	private final String[] spectatorSpawnKeys;

	private final List<MapRegion> participantSpawnRegions = new ArrayList<>();
	private final List<MapRegion> spectatorSpawnRegions = new ArrayList<>();

	private int participantSpawnIndex;
	private int spectatorSpawnIndex;

	public PositionPlayersMinigameBehavior(final String[] participantSpawnKeys, String[] spectatorSpawnKeys) {
		this.participantSpawnKeys = participantSpawnKeys;
		this.spectatorSpawnKeys = spectatorSpawnKeys;
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		MapRegions regions = minigame.getMapRegions();

		participantSpawnRegions.clear();
		spectatorSpawnRegions.clear();

		for (String key : participantSpawnKeys) {
			participantSpawnRegions.addAll(regions.get(key));
		}

		for (String key : spectatorSpawnKeys) {
			spectatorSpawnRegions.addAll(regions.get(key));
		}
	}

	public static <T> PositionPlayersMinigameBehavior parse(Dynamic<T> root) {
		String[] participantSpawns = root.get("participants").asList(d -> d.asString("")).toArray(new String[0]);
		String[] spectatorSpawns = root.get("spectators").asList(d -> d.asString("")).toArray(new String[0]);
		return new PositionPlayersMinigameBehavior(participantSpawns, spectatorSpawns);
	}

	@Override
	public void onPlayerJoin(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		setupPlayerAsRole(minigame, player, role);
	}

	@Override
	public void onPlayerChangeRole(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		setupPlayerAsRole(minigame, player, role);
	}

	private void setupPlayerAsRole(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		if (role == PlayerRole.PARTICIPANT) {
			MapRegion region = participantSpawnRegions.get(participantSpawnIndex++ % participantSpawnRegions.size());
			teleportToRegion(minigame, player, region);
		} else {
			MapRegion region = spectatorSpawnRegions.get(spectatorSpawnIndex++ % spectatorSpawnRegions.size());
			teleportToRegion(minigame, player, region);
		}
	}

	private void teleportToRegion(IMinigameInstance minigame, ServerPlayerEntity player, MapRegion region) {
		BlockPos pos = region.sample(player.getRNG());
		DimensionUtils.teleportPlayerNoPortal(player, minigame.getDimension(), pos);
	}
}
