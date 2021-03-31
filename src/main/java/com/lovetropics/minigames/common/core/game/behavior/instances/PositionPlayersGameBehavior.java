package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.map.MapRegion;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.util.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class PositionPlayersGameBehavior implements IGameBehavior {
	public static final Codec<PositionPlayersGameBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).fieldOf("participants").forGetter(c -> c.participantSpawnKeys),
				MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).fieldOf("spectators").forGetter(c -> c.spectatorSpawnKeys)
		).apply(instance, PositionPlayersGameBehavior::new);
	});

	private final String[] participantSpawnKeys;
	private final String[] spectatorSpawnKeys;

	private final List<MapRegion> participantSpawnRegions = new ArrayList<>();
	private final List<MapRegion> spectatorSpawnRegions = new ArrayList<>();

	private int participantSpawnIndex;
	private int spectatorSpawnIndex;

	public PositionPlayersGameBehavior(final String[] participantSpawnKeys, String[] spectatorSpawnKeys) {
		this.participantSpawnKeys = participantSpawnKeys;
		this.spectatorSpawnKeys = spectatorSpawnKeys;
	}

	@Override
	public void register(IGameInstance registerGame, EventRegistrar events) {
		MapRegions regions = registerGame.getMapRegions();

		participantSpawnRegions.clear();
		spectatorSpawnRegions.clear();

		for (String key : participantSpawnKeys) {
			participantSpawnRegions.addAll(regions.get(key));
		}

		for (String key : spectatorSpawnKeys) {
			spectatorSpawnRegions.addAll(regions.get(key));
		}

		events.listen(GamePlayerEvents.JOIN, this::setupPlayerAsRole);
		events.listen(GamePlayerEvents.CHANGE_ROLE, (game, player, role, lastRole) -> setupPlayerAsRole(game, player, role));
	}

	private void setupPlayerAsRole(IGameInstance game, ServerPlayerEntity player, PlayerRole role) {
		if (role == PlayerRole.PARTICIPANT) {
			MapRegion region = participantSpawnRegions.get(participantSpawnIndex++ % participantSpawnRegions.size());
			teleportToRegion(game, player, region);
		} else {
			MapRegion region = spectatorSpawnRegions.get(spectatorSpawnIndex++ % spectatorSpawnRegions.size());
			teleportToRegion(game, player, region);
		}
	}

	private void teleportToRegion(IGameInstance minigame, ServerPlayerEntity player, MapRegion region) {
		BlockPos pos = region.sample(player.getRNG());
		DimensionUtils.teleportPlayerNoPortal(player, minigame.getDimension(), pos);
	}
}
