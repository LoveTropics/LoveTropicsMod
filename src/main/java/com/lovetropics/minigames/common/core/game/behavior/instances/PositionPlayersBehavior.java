package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PositionPlayersBehavior implements IGameBehavior {
	public static final Codec<PositionPlayersBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).optionalFieldOf("participants", new String[0]).forGetter(c -> c.participantSpawnKeys),
			MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).optionalFieldOf("spectators", new String[0]).forGetter(c -> c.spectatorSpawnKeys),
			MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).optionalFieldOf("all", new String[0]).forGetter(c -> c.allSpawnKeys)
	).apply(i, PositionPlayersBehavior::new));

	private final String[] participantSpawnKeys;
	private final String[] spectatorSpawnKeys;
	private final String[] allSpawnKeys;

	private final List<BlockBox> participantSpawnRegions = new ArrayList<>();
	private final List<BlockBox> spectatorSpawnRegions = new ArrayList<>();
	private final List<BlockBox> allSpawnRegions = new ArrayList<>();

	private int participantSpawnIndex;
	private int spectatorSpawnIndex;
	private int allSpawnIndex;

	public PositionPlayersBehavior(String[] participantSpawnKeys, String[] spectatorSpawnKeys, String[] allSpawnKeys) {
		this.participantSpawnKeys = participantSpawnKeys;
		this.spectatorSpawnKeys = spectatorSpawnKeys;
		this.allSpawnKeys = allSpawnKeys;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		MapRegions regions = game.getMapRegions();

		participantSpawnRegions.clear();
		spectatorSpawnRegions.clear();
		allSpawnRegions.clear();

		for (String key : participantSpawnKeys) {
			participantSpawnRegions.addAll(regions.get(key));
		}
		LogManager.getLogger().info("FOUND " + participantSpawnRegions.size() + " PARTICIPANT SPAWN REGIONS");

		for (String key : spectatorSpawnKeys) {
			spectatorSpawnRegions.addAll(regions.get(key));
		}

		for (String key : allSpawnKeys) {
			allSpawnRegions.addAll(regions.get(key));
		}

		events.listen(GamePlayerEvents.SPAWN, (player, role) -> spawnPlayerAsRole(game, player, role));
	}

	private void spawnPlayerAsRole(IGamePhase game, ServerPlayer player, @Nullable PlayerRole role) {
		BlockBox region = getSpawnRegionFor(role);
		if (region != null) {
			teleportToRegion(game, player, region);
		}
	}

	@Nullable
	private BlockBox getSpawnRegionFor(PlayerRole role) {
		if (role == PlayerRole.PARTICIPANT && !participantSpawnRegions.isEmpty()) {
			LogManager.getLogger().info("USING REGION INDEX " + participantSpawnIndex);
			return participantSpawnRegions.get(participantSpawnIndex++ % participantSpawnRegions.size());
		} else if (role == PlayerRole.SPECTATOR && !spectatorSpawnRegions.isEmpty()) {
			return spectatorSpawnRegions.get(spectatorSpawnIndex++ % spectatorSpawnRegions.size());
		} else if (!allSpawnRegions.isEmpty()) {
			return allSpawnRegions.get(allSpawnIndex++ % allSpawnRegions.size());
		}
		return null;
	}

	private void teleportToRegion(IGamePhase game, ServerPlayer player, BlockBox region) {
		BlockPos pos = tryFindEmptyPos(game, player.getRandom(), region);
		DimensionUtils.teleportPlayerNoPortal(player, game.getDimension(), pos);
	}

	private BlockPos tryFindEmptyPos(IGamePhase game, RandomSource random, BlockBox box) {
		ServerLevel world = game.getWorld();
		for (int i = 0; i < 20; i++) {
			BlockPos pos = box.sample(random);
			if (world.isEmptyBlock(pos)) {
				return pos;
			}
		}
		LogManager.getLogger().info("USING FALLBACK SPAWN POS");
		return box.centerBlock();
	}
}
