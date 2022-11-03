package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.lovetropics.minigames.common.core.integration.GameInstanceTelemetry;
import com.lovetropics.minigames.common.core.integration.Telemetry;
import com.mojang.serialization.Codec;

import java.util.concurrent.atomic.AtomicBoolean;

public final class SetupTelemetryBehavior implements IGameBehavior {
	public static final Codec<SetupTelemetryBehavior> CODEC = Codec.unit(SetupTelemetryBehavior::new);

	private GameInstanceTelemetry telemetry;

	// TODO: we could potentially have state entries & the IGamePhase come through the constructor with codec hacks
	@Override
	public void registerState(IGamePhase game, GameStateMap state) {
		if (game.getLobby().getMetadata().visibility().isFocusedLive()) {
			if (!Telemetry.INSTANCE.isConnected()) {
				throw new GameException(GameTexts.Status.telemetryNotConnected());
			}

			telemetry = state.register(GameInstanceTelemetry.KEY, Telemetry.INSTANCE.openGame(game));
		}
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		if (telemetry == null) {
			return;
		}

		events.listen(GamePhaseEvents.START, () -> telemetry.start(events));

		AtomicBoolean finished = new AtomicBoolean();
		events.listen(GameLogicEvents.GAME_OVER, () -> {
			if (finished.compareAndSet(false, true)) {
				telemetry.finish(game.getStatistics());
			}
		});

		events.listen(GamePhaseEvents.STOP, reason -> {
			if (!finished.compareAndSet(false, true)) {
				return;
			}
			if (reason == GameStopReason.finished()) {
				telemetry.finish(game.getStatistics());
			} else {
				telemetry.cancel();
			}
		});
	}
}
