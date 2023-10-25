package com.lovetropics.minigames.common.core.game.behavior.instances.team;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.rewards.GameRewardsMap;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableBoolean;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public record TeamWinTrigger(List<ItemStack> rewards) implements IGameBehavior {
	public static final MapCodec<TeamWinTrigger> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MoreCodecs.ITEM_STACK.listOf().optionalFieldOf("rewards", List.of()).forGetter(TeamWinTrigger::rewards)
	).apply(i, TeamWinTrigger::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		TeamState teamState = game.getInstanceState().getOrThrow(TeamState.KEY);
		GameRewardsMap rewardsMap = game.getState().getOrThrow(GameRewardsMap.STATE);

		MutableBoolean winTriggered = new MutableBoolean();

		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> {
			if (lastRole != PlayerRole.PARTICIPANT || winTriggered.isTrue()) {
				return;
			}

			GameTeamKey playerTeam = teamState.getTeamForPlayer(player);
			if (playerTeam == null) {
				return;
			}

			if (teamState.getParticipantsForTeam(playerTeam).isEmpty()) {
				GameTeam finalTeam = getFinalTeam(teamState);
				if (finalTeam == null) {
					// How did we get here? If there are no other teams, the team who died last is probably the winner
					finalTeam = Objects.requireNonNull(teamState.getTeamByKey(playerTeam));
				}

				winTriggered.setTrue();

				Component winnerName = finalTeam.config().name().copy()
						.withStyle(finalTeam.config().formatting());
				game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(winnerName);
				game.invoker(GameLogicEvents.GAME_OVER).onGameOver();

				game.getStatistics().global().set(StatisticKey.WINNING_TEAM, finalTeam.key());

				for (ServerPlayer teamPlayer : teamState.getPlayersForTeam(finalTeam.key())) {
					for (ItemStack reward : rewards) {
						rewardsMap.give(teamPlayer, reward);
					}
				}
			}
		});
	}

	@Nullable
	private GameTeam getFinalTeam(TeamState teamState) {
		GameTeam finalTeam = null;
		for (GameTeam team : teamState) {
			if (teamState.getParticipantsForTeam(team.key()).isEmpty()) {
				continue;
			}

			if (finalTeam != null) {
				return null;
			} else {
				finalTeam = team;
			}
		}

		return finalTeam;
	}
}
