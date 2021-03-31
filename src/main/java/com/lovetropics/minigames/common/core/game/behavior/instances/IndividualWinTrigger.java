package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerSet;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;

public class IndividualWinTrigger implements IGameBehavior {
	public static final Codec<IndividualWinTrigger> CODEC = Codec.unit(IndividualWinTrigger::new);

	@Override
	public void register(IGameInstance registerGame, EventRegistrar events) throws GameException {
		events.listen(GamePlayerEvents.DEATH, (game, player, damageSource) -> {
			PlayerSet participants = game.getParticipants();
			if (participants.size() == 1) {
				ServerPlayerEntity winningPlayer = participants.iterator().next();

				game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(game, winningPlayer.getDisplayName());
				game.invoker(GameLogicEvents.GAME_OVER).onGameOver(game);

				game.getStatistics().getGlobal().set(StatisticKey.WINNING_PLAYER, PlayerKey.from(winningPlayer));
			}

			return ActionResultType.PASS;
		});
	}
}
