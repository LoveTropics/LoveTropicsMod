package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PositionPlayersBehavior implements IGameBehavior {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final Codec<PositionPlayersBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).optionalFieldOf("participants", new String[0]).forGetter(c -> c.participantSpawnKeys),
			MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).optionalFieldOf("spectators", new String[0]).forGetter(c -> c.spectatorSpawnKeys),
			MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).optionalFieldOf("all", new String[0]).forGetter(c -> c.allSpawnKeys),
			Codec.BOOL.optionalFieldOf("split_by_team", true).forGetter(c -> c.splitByTeam)
	).apply(i, PositionPlayersBehavior::new));

	private final String[] participantSpawnKeys;
	private final String[] spectatorSpawnKeys;
	private final String[] allSpawnKeys;
	private final boolean splitByTeam;

	private CycledSpawner participantSpawner = CycledSpawner.EMPTY;
	private CycledSpawner spectatorSpawner = CycledSpawner.EMPTY;
	private CycledSpawner fallbackSpawner = CycledSpawner.EMPTY;
	private Map<GameTeamKey, CycledSpawner> teamSpawners = Map.of();

	public PositionPlayersBehavior(String[] participantSpawnKeys, String[] spectatorSpawnKeys, String[] allSpawnKeys, boolean splitByTeam) {
		this.participantSpawnKeys = participantSpawnKeys;
		this.spectatorSpawnKeys = spectatorSpawnKeys;
		this.allSpawnKeys = allSpawnKeys;
		this.splitByTeam = splitByTeam;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		MapRegions regions = game.getMapRegions();

		participantSpawner = new CycledSpawner(regions, participantSpawnKeys);
		spectatorSpawner = new CycledSpawner(regions, spectatorSpawnKeys);
		fallbackSpawner = new CycledSpawner(regions, allSpawnKeys);
		LOGGER.debug("FOUND " + participantSpawner.regions.size() + " PARTICIPANT SPAWN REGIONS");

		TeamState teams = game.getState().getOrNull(TeamState.KEY);
		if (splitByTeam && teams != null) {
			events.listen(GamePhaseEvents.CREATE, () -> {
				int participantCount = game.getParticipants().size();
				teamSpawners = createTeamSpawners(teams, participantSpawner, participantCount);
			});
		}

		events.listen(GamePlayerEvents.SPAWN, (player, role) -> spawnPlayerAsRole(game, player, role, teams));
	}

	private Map<GameTeamKey, CycledSpawner> createTeamSpawners(TeamState teams, CycledSpawner spawns, int participantCount) {
		Map<GameTeamKey, CycledSpawner> teamSpawners = new HashMap<>();

		int spawnCount = spawns.size();
		int groupSize = Math.max(participantCount / spawnCount, 1);
		for (GameTeam team : teams) {
			PlayerSet teamPlayers = teams.getPlayersForTeam(team.key());
			int teamSize = teamPlayers.size();
			int teamGroupCount = Math.max(teamSize / groupSize, 1);
			teamSpawners.put(team.key(), spawns.take(teamGroupCount));
		}

		return teamSpawners;
	}

	private void spawnPlayerAsRole(IGamePhase game, ServerPlayer player, @Nullable PlayerRole role, @Nullable TeamState teams) {
		BlockBox region = getSpawnRegionFor(player, role, teams);
		if (region != null) {
			teleportToRegion(game, player, region);
		}
	}

	@Nullable
	private BlockBox getSpawnRegionFor(ServerPlayer player, PlayerRole role, @Nullable TeamState teams) {
		if (role == PlayerRole.PARTICIPANT) {
			BlockBox region = getParticipantSpawnRegion(player, teams);
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

	@Nullable
	private BlockBox getParticipantSpawnRegion(ServerPlayer player, TeamState teams) {
		GameTeamKey team = teams != null ? teams.getTeamForPlayer(player) : null;
		if (team != null) {
			CycledSpawner teamSpawner = teamSpawners.get(team);
			if (teamSpawner != null) {
				return teamSpawner.next();
			}
		}
		return participantSpawner.next();
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
			this.regions = new ArrayList<>(regions);
			Collections.shuffle(this.regions);
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

		public CycledSpawner take(int count) {
			List<BlockBox> result = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				BlockBox region = next();
				if (region == null) {
					break;
				}
				result.add(region);
			}
			return new CycledSpawner(result);
		}

		public int size() {
			return regions.size();
		}
	}
}
