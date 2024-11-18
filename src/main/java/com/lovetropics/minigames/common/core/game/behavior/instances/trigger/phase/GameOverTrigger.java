package com.lovetropics.minigames.common.core.game.behavior.instances.trigger.phase;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameWinner;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;

public record GameOverTrigger(GameActionList<ServerPlayer> actions) implements IGameBehavior {
	public static final MapCodec<GameOverTrigger> CODEC = GameActionList.PLAYER_MAP_CODEC
			.xmap(GameOverTrigger::new, GameOverTrigger::actions);

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		actions.register(game, events);
		events.listen(GameLogicEvents.GAME_OVER, winner -> {
			GameActionContext context = GameActionContext.builder().set(GameActionParameter.WINNER, winner.name()).build();
			if (winner instanceof GameWinner.Player(ServerPlayer player)) {
				actions.apply(game, context, player);
			} else {
				actions.apply(game, context);
			}
		});
	}
}
