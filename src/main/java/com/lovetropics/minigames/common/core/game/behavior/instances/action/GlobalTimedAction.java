package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerPlayer;

public record GlobalTimedAction(GameActionList<ServerPlayer> apply, GameActionList<ServerPlayer> clear, int seconds) implements IGameBehavior {
	public static final MapCodec<GlobalTimedAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			GameActionList.PLAYER_CODEC.fieldOf("apply").forGetter(GlobalTimedAction::apply),
			GameActionList.PLAYER_CODEC.fieldOf("clear").forGetter(GlobalTimedAction::clear),
			Codec.INT.fieldOf("seconds").forGetter(GlobalTimedAction::seconds)
	).apply(i, GlobalTimedAction::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		apply.register(game, events);
		clear.register(game, events);

		final State state = new State();
		events.listen(GameActionEvents.APPLY, context -> state.tryApply(game, context));
		events.listen(GamePhaseEvents.TICK, () -> state.tick(game));
	}

	private class State {
		private static final long NOT_ACTIVE = -1;

		private long finishTime = NOT_ACTIVE;

		private void tick(final IGamePhase game) {
			if (finishTime != NOT_ACTIVE && game.ticks() >= finishTime) {
				disable(game);
				finishTime = NOT_ACTIVE;
			}
		}

		private boolean tryApply(final IGamePhase game, final GameActionContext context) {
			if (finishTime == NOT_ACTIVE) {
				apply(game, context);
				finishTime = game.ticks() + (long) seconds * SharedConstants.TICKS_PER_SECOND;
				return true;
			}
			return false;
		}

		private boolean apply(final IGamePhase game, final GameActionContext context) {
			return apply.apply(game, context);
		}

		private void disable(final IGamePhase game) {
			clear.apply(game, GameActionContext.EMPTY);
		}
	}
}
