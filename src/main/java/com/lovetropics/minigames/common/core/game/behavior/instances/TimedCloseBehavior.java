
package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.GameProgressionState;
import com.lovetropics.minigames.common.core.game.state.ProgressionPoint;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.apache.commons.lang3.mutable.MutableBoolean;

public record TimedCloseBehavior(ProgressionPoint end, ProgressionPoint close) implements IGameBehavior {
	public static final MapCodec<TimedCloseBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ProgressionPoint.CODEC.fieldOf("end").forGetter(TimedCloseBehavior::end),
			ProgressionPoint.CODEC.fieldOf("close").forGetter(TimedCloseBehavior::close)
	).apply(i, TimedCloseBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		final GameProgressionState progression = game.state().getOrThrow(GameProgressionState.KEY);
		final MutableBoolean gameOver = new MutableBoolean();
		events.listen(GamePhaseEvents.TICK, () -> {
			if (!gameOver.getValue() && progression.isAfter(end)) {
				game.invoker(GameLogicEvents.GAME_OVER).onGameOver();
				gameOver.setTrue();
			}
			if (progression.isAfter(close)) {
				game.requestStop(GameStopReason.finished());
			}
		});
	}
}
