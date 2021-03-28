package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.MoreCodecs;
import com.lovetropics.minigames.common.dimension.DimensionUtils;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class PositionPlayersMinigameBehavior implements IMinigameBehavior {
	public static final Codec<PositionPlayersMinigameBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).fieldOf("participants").forGetter(c -> c.participantSpawnKeys),
				MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).fieldOf("spectators").forGetter(c -> c.spectatorSpawnKeys)
		).apply(instance, PositionPlayersMinigameBehavior::new);
	});

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

	@Override
	public void onPlayerJoin(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		setupPlayerAsRole(minigame, player, role);
	}

	@Override
	public void onPlayerChangeRole(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
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
