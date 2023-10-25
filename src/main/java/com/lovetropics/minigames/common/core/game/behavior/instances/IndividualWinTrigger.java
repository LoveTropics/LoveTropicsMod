package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.rewards.GameRewardsMap;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record IndividualWinTrigger(List<ItemStack> rewards) implements IGameBehavior {
	public static final MapCodec<IndividualWinTrigger> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MoreCodecs.ITEM_STACK.listOf().optionalFieldOf("rewards", List.of()).forGetter(IndividualWinTrigger::rewards)
	).apply(i, IndividualWinTrigger::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		GameRewardsMap rewardsMap = game.getState().getOrThrow(GameRewardsMap.STATE);

		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> {
			if (lastRole != PlayerRole.PARTICIPANT) {
				return;
			}

			PlayerSet participants = game.getParticipants();
			if (participants.size() == 1) {
				ServerPlayer winningPlayer = participants.iterator().next();

				game.getStatistics().global().set(StatisticKey.WINNING_PLAYER, PlayerKey.from(winningPlayer));

				game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(winningPlayer.getDisplayName());
				game.invoker(GameLogicEvents.GAME_OVER).onGameOver();

				for (ItemStack reward : rewards) {
					rewardsMap.give(winningPlayer, reward);
				}
			}
		});
	}
}
