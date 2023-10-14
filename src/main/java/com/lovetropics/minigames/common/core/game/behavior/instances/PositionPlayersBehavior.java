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
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;

public class PositionPlayersBehavior implements IGameBehavior {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final Codec<PositionPlayersBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).optionalFieldOf("participants", new String[0]).forGetter(c -> c.participantSpawnKeys),
			MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).optionalFieldOf("spectators", new String[0]).forGetter(c -> c.spectatorSpawnKeys),
			MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).fieldOf("all").forGetter(c -> c.allSpawnKeys)
	).apply(i, PositionPlayersBehavior::new));

	private final String[] participantSpawnKeys;
	private final String[] spectatorSpawnKeys;
	private final String[] allSpawnKeys;

	private CycledSpawner participantSpawner = CycledSpawner.EMPTY;
	private CycledSpawner spectatorSpawner = CycledSpawner.EMPTY;
	private CycledSpawner fallbackSpawner = CycledSpawner.EMPTY;

	public PositionPlayersBehavior(String[] participantSpawnKeys, String[] spectatorSpawnKeys, String[] allSpawnKeys) {
		this.participantSpawnKeys = participantSpawnKeys;
		this.spectatorSpawnKeys = spectatorSpawnKeys;
		this.allSpawnKeys = allSpawnKeys;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		MapRegions regions = game.getMapRegions();

		participantSpawner = new CycledSpawner(regions, participantSpawnKeys);
		spectatorSpawner = new CycledSpawner(regions, spectatorSpawnKeys);
		fallbackSpawner = new CycledSpawner(regions, allSpawnKeys);
		LOGGER.debug("FOUND " + participantSpawner.regions.size() + " PARTICIPANT SPAWN REGIONS");

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
		if (role == PlayerRole.PARTICIPANT) {
			BlockBox region = participantSpawner.next();
			if (region != null) {
				return region;
			}
		} else if (role == PlayerRole.SPECTATOR) {
			BlockBox region = spectatorSpawner.next();
			if (region != null) {
				return region;
			}
		}
		return fallbackSpawner.next();
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
		LOGGER.debug("USING FALLBACK SPAWN POS");
		return box.centerBlock();
	}

	private static class CycledSpawner {
		public static final CycledSpawner EMPTY = new CycledSpawner(List.of());

		private final List<BlockBox> regions;
		private int index;

		public CycledSpawner(List<BlockBox> regions) {
			this.regions = regions;
		}
		
		public CycledSpawner(MapRegions regions, String... keys) {
			this(regions.getAll(keys));
		}

		@Nullable
		public BlockBox next() {
			if (regions.isEmpty()) {
				return null;
			}
			return regions.get(index++ % regions.size());
		}
	}
}
