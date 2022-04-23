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
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;

public class IndividualWinTrigger implements IGameBehavior {
	public static final Codec<IndividualWinTrigger> CODEC = Codec.unit(IndividualWinTrigger::new);

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> {
			if (lastRole != PlayerRole.PARTICIPANT) {
				return;
			}

			PlayerSet participants = game.getParticipants();
			if (participants.size() == 1) {
				ServerPlayer winningPlayer = participants.iterator().next();

				game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(winningPlayer.getDisplayName());
				game.invoker(GameLogicEvents.GAME_OVER).onGameOver();

				game.getStatistics().global().set(StatisticKey.WINNING_PLAYER, PlayerKey.from(winningPlayer));
			}
		});
	}
}
