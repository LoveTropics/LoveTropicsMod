package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.river_race.event.RiverRaceEvents;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.SpawnBuilder;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.SubGameEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public record RiverRaceSpawnsBehavior(
		Map<String, ZoneSpawns> zoneSpawns
) implements IGameBehavior {
	public static final MapCodec<RiverRaceSpawnsBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.unboundedMap(Codec.STRING, ZoneSpawns.CODEC).fieldOf("zone_spawns").forGetter(RiverRaceSpawnsBehavior::zoneSpawns)
	).apply(i, RiverRaceSpawnsBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		TeamState teams = game.instanceState().getOrThrow(TeamState.KEY);
		Map<GameTeamKey, Spawn> currentSpawns = new HashMap<>();

		zoneSpawns.values().stream().filter(ZoneSpawns::unlockOnStart).findFirst()
				.ifPresent(spawns -> resolveSpawns(game, currentSpawns, spawns));

		events.listen(GamePlayerEvents.SPAWN, (playerId, spawn, role) -> {
			GameTeamKey team = teams.getTeamForPlayer(playerId);
			if (team != null) {
				Spawn teamSpawn = currentSpawns.get(team);
				if (teamSpawn != null) {
					BlockPos pos = teamSpawn.box.sample(game.random());
					spawn.teleportTo(game.level(), pos, teamSpawn.angle());
				}
			}
		});

		events.listen(RiverRaceEvents.UNLOCK_ZONE, id -> {
			ZoneSpawns newSpawns = zoneSpawns.get(id);
			if (newSpawns != null) {
				resolveSpawns(game, currentSpawns, newSpawns);
			}
		});

		events.listen(SubGameEvents.RETURN_TO_TOP, () -> {
			// Bring all players to the new zone
			for (ServerPlayer player : game.participants()) {
				SpawnBuilder spawn = new SpawnBuilder(player);
				game.invoker(GamePlayerEvents.SPAWN).onSpawn(player.getUUID(), spawn, PlayerRole.PARTICIPANT);
				spawn.teleportAndApply(player);
			}
		});
	}

	private void resolveSpawns(IGamePhase game, Map<GameTeamKey, Spawn> output, ZoneSpawns spawns) {
		output.clear();
		spawns.regionByTeam.forEach((team, region) -> {
			BlockBox box = game.mapRegions().getOrThrow(region);
			output.put(team, new Spawn(box, spawns.angle));
		});
	}

	private record Spawn(BlockBox box, float angle) {
	}

	public record ZoneSpawns(boolean unlockOnStart, Map<GameTeamKey, String> regionByTeam, float angle) {
		public static final Codec<ZoneSpawns> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.BOOL.optionalFieldOf("unlock_on_start", false).forGetter(ZoneSpawns::unlockOnStart),
				Codec.unboundedMap(GameTeamKey.CODEC, Codec.STRING).fieldOf("region_by_team").forGetter(ZoneSpawns::regionByTeam),
				Codec.FLOAT.optionalFieldOf("angle", 0.0f).forGetter(ZoneSpawns::angle)
		).apply(i, ZoneSpawns::new));
	}
}
