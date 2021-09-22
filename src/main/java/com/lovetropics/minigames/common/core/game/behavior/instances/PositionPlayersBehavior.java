package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class PositionPlayersBehavior implements IGameBehavior {
	public static final Codec<PositionPlayersBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).fieldOf("participants").forGetter(c -> c.participantSpawnKeys),
				MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).fieldOf("spectators").forGetter(c -> c.spectatorSpawnKeys)
		).apply(instance, PositionPlayersBehavior::new);
	});

	private final String[] participantSpawnKeys;
	private final String[] spectatorSpawnKeys;

	private final List<BlockBox> participantSpawnRegions = new ArrayList<>();
	private final List<BlockBox> spectatorSpawnRegions = new ArrayList<>();

	private int participantSpawnIndex;
	private int spectatorSpawnIndex;

	public PositionPlayersBehavior(final String[] participantSpawnKeys, String[] spectatorSpawnKeys) {
		this.participantSpawnKeys = participantSpawnKeys;
		this.spectatorSpawnKeys = spectatorSpawnKeys;
	}

	@Override
	public void register(IActiveGame registerGame, EventRegistrar events) {
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

	private void setupPlayerAsRole(IActiveGame game, ServerPlayerEntity player, PlayerRole role) {
		if (role == PlayerRole.PARTICIPANT) {
			BlockBox region = participantSpawnRegions.get(participantSpawnIndex++ % participantSpawnRegions.size());
			teleportToRegion(game, player, region);
		} else {
			BlockBox region = spectatorSpawnRegions.get(spectatorSpawnIndex++ % spectatorSpawnRegions.size());
			teleportToRegion(game, player, region);
		}
	}

	private void teleportToRegion(IActiveGame game, ServerPlayerEntity player, BlockBox region) {
		BlockPos pos = region.sample(player.getRNG());
		DimensionUtils.teleportPlayerNoPortal(player, game.getDimension(), pos);
	}
}
