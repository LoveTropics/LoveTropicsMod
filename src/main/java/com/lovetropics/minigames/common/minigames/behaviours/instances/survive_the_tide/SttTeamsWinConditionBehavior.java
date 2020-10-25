package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.VariableText;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.lovetropics.minigames.common.minigames.behaviours.instances.TeamsBehavior;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import javax.annotation.Nullable;
import java.util.Optional;

public class SttTeamsWinConditionBehavior extends SttWinConditionBehavior {
	public SttTeamsWinConditionBehavior(final long gameFinishTickDelay, final Long2ObjectMap<VariableText> scheduledGameFinishMessages, final boolean spawnLightningBoltsOnFinish, final int lightningBoltSpawnTickRate) {
		super(gameFinishTickDelay, scheduledGameFinishMessages, spawnLightningBoltsOnFinish, lightningBoltSpawnTickRate);
	}

	public static <T> SttTeamsWinConditionBehavior parse(Dynamic<T> root) {
		final long gameFinishTickDelay = root.get("game_finish_tick_delay").asLong(0);
		final Long2ObjectMap<VariableText> scheduledShutdownMessages = new Long2ObjectOpenHashMap<>(root.get("scheduled_game_finish_messages").asMap(
				key -> Long.parseLong(key.asString("0")),
				VariableText::parse
		));

		final boolean spawnLightningBoltsOnFinish = root.get("spawn_lightning_bolts_on_finish").asBoolean(false);
		final int lightningBoltSpawnTickRate = root.get("lightning_bolt_spawn_tick_rate").asInt(60);

		return new SttTeamsWinConditionBehavior(gameFinishTickDelay, scheduledShutdownMessages, spawnLightningBoltsOnFinish, lightningBoltSpawnTickRate);
	}

	@Override
	public void onPlayerDeath(final IMinigameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		if (minigameEnded) {
			return;
		}

		Optional<TeamsBehavior> teamsBehaviorOpt = minigame.getBehavior(MinigameBehaviorTypes.TEAMS.get());
		if (!teamsBehaviorOpt.isPresent()) {
			return;
		}

		TeamsBehavior teamsBehavior = teamsBehaviorOpt.get();

		TeamsBehavior.TeamKey playerTeam = teamsBehavior.getTeamForPlayer(player);
		if (teamsBehavior.getPlayersForTeam(playerTeam).isEmpty()) {
			TeamsBehavior.TeamKey lastTeam = getLastTeam(teamsBehavior);
			if (lastTeam != null) {
				triggerWin(new StringTextComponent(lastTeam.name).applyTextStyle(lastTeam.text));
				minigame.getStatistics().getGlobal().set(StatisticKey.WINNING_TEAM, lastTeam);
			}
		}
	}

	@Nullable
	private TeamsBehavior.TeamKey getLastTeam(TeamsBehavior teamBehavior) {
		TeamsBehavior.TeamKey lastTeam = null;
		for (TeamsBehavior.TeamKey team : teamBehavior.getTeams()) {
			if (teamBehavior.getPlayersForTeam(team).isEmpty()) {
				continue;
			}

			if (lastTeam != null) {
				return null;
			} else {
				lastTeam = team;
			}
		}

		return lastTeam;
	}
}
