
package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.GameWinner;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.progress.ProgressChannel;
import com.lovetropics.minigames.common.core.game.state.progress.ProgressHolder;
import com.lovetropics.minigames.common.core.game.state.progress.ProgressionPoint;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.Optional;

public record TimedCloseBehavior(ProgressChannel channel, Optional<ProgressionPoint> end, Optional<ProgressionPoint> close) implements IGameBehavior {
	public static final MapCodec<TimedCloseBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ProgressChannel.CODEC.optionalFieldOf("channel", ProgressChannel.MAIN).forGetter(TimedCloseBehavior::channel),
			ProgressionPoint.CODEC.optionalFieldOf("end").forGetter(TimedCloseBehavior::end),
			ProgressionPoint.CODEC.optionalFieldOf("close").forGetter(TimedCloseBehavior::close)
	).apply(i, TimedCloseBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		final ProgressHolder progression = channel.getOrThrow(game);
		final MutableBoolean gameOver = new MutableBoolean();
		events.listen(GamePhaseEvents.TICK, () -> {
			if (end.isPresent() && !gameOver.getValue() && progression.isAfter(end.get())) {
				if (!game.invoker(GameLogicEvents.REQUEST_GAME_OVER).requestGameOver()) {
					game.invoker(GameLogicEvents.GAME_OVER).onGameOver(new GameWinner.Nobody());
				}
				gameOver.setTrue();
			}
			if (close.isPresent() && progression.isAfter(close.get())) {
				game.requestStop(GameStopReason.finished());
			}
		});
	}
}
