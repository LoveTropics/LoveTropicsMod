package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;

public class IndividualWinTrigger implements IGameBehavior {
	public static final MapCodec<IndividualWinTrigger> CODEC = MapCodec.unit(IndividualWinTrigger::new);

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> {
			if (lastRole != PlayerRole.PARTICIPANT) {
				return;
			}

			PlayerSet participants = game.participants();
			if (participants.size() == 1) {
				ServerPlayer winningPlayer = participants.iterator().next();

				game.statistics().global().set(StatisticKey.WINNING_PLAYER, PlayerKey.from(winningPlayer));

				game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(winningPlayer.getDisplayName());
				game.invoker(GameLogicEvents.GAME_OVER).onGameOver();
			}
		});
	}
}
