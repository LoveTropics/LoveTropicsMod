package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.dimension.DimensionUtils;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
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
	public void onConstruct(IMinigameInstance minigame, MinecraftServer server) {
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

	@Override
	public void onPlayerJoin(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		if (role == PlayerRole.PARTICIPANT) {
			if (participantSpawnKeys.length != minigame.getDefinition().getMaximumParticipantCount()) {
				throw new IllegalStateException("The participant positions length doesn't match the" +
						"maximum participant count defined by the following minigame definition! " + minigame.getDefinition().getID());
			}

			MapRegion region = participantSpawnRegions.get(participantSpawnIndex++ % participantSpawnRegions.size());
			teleportToRegion(minigame, player, region);
		} else {
			MapRegion region = spectatorSpawnRegions.get(spectatorSpawnIndex++ % spectatorSpawnRegions.size());
			teleportToRegion(minigame, player, region);
		}
	}

	private void teleportToRegion(IMinigameInstance minigame, ServerPlayerEntity player, MapRegion region) {
		BlockPos pos = new BlockPos(region.getCenter());
		DimensionUtils.teleportPlayerNoPortal(player, minigame.getDimension(), pos);
	}
}
