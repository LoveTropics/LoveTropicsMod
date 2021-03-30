package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.behavior.instances.TeamsBehavior;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.lovetropics.minigames.common.util.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.Optional;

public class SttTeamsWinConditionBehavior extends SttWinConditionBehavior {
	public static final Codec<SttTeamsWinConditionBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.LONG.optionalFieldOf("game_finish_tick_delay", 0L).forGetter(c -> c.gameFinishTickDelay),
				MoreCodecs.long2Object(TemplatedText.CODEC).fieldOf("scheduled_game_finish_messages").forGetter(c -> c.scheduledGameFinishMessages),
				Codec.BOOL.optionalFieldOf("spawn_lightning_bolts_on_finish", false).forGetter(c -> c.spawnLightningBoltsOnFinish),
				Codec.INT.optionalFieldOf("lightning_bolt_spawn_tick_rate", 60).forGetter(c -> c.lightningBoltSpawnTickRate)
		).apply(instance, SttTeamsWinConditionBehavior::new);
	});

	public SttTeamsWinConditionBehavior(final long gameFinishTickDelay, final Long2ObjectMap<TemplatedText> scheduledGameFinishMessages, final boolean spawnLightningBoltsOnFinish, final int lightningBoltSpawnTickRate) {
		super(gameFinishTickDelay, scheduledGameFinishMessages, spawnLightningBoltsOnFinish, lightningBoltSpawnTickRate);
	}

	@Override
	public void register(IGameInstance registerGame, GameEventListeners events) throws GameException {
		super.register(registerGame, events);

		events.listen(GamePlayerEvents.DEATH, (game, player, damageSource) -> {
			if (minigameEnded) {
				return ActionResultType.PASS;
			}

			Optional<TeamsBehavior> teamsBehaviorOpt = game.getOneBehavior(GameBehaviorTypes.TEAMS.get());
			if (!teamsBehaviorOpt.isPresent()) {
				return ActionResultType.PASS;
			}

			TeamsBehavior teamsBehavior = teamsBehaviorOpt.get();

			TeamsBehavior.TeamKey playerTeam = teamsBehavior.getTeamForPlayer(player);
			if (teamsBehavior.getPlayersForTeam(playerTeam).isEmpty()) {
				TeamsBehavior.TeamKey lastTeam = getLastTeam(teamsBehavior);
				if (lastTeam != null) {
					triggerWin(new StringTextComponent(lastTeam.name).mergeStyle(lastTeam.text));
					game.getStatistics().getGlobal().set(StatisticKey.WINNING_TEAM, lastTeam);
				}
			}

			return ActionResultType.PASS;
		});
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
