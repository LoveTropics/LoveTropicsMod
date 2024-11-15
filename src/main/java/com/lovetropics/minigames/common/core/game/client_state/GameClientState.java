package com.lovetropics.minigames.common.core.game.client_state;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public interface GameClientState {
	Codec<GameClientState> CODEC = GameClientStateTypes.TYPE_CODEC.dispatch(
			"type",
			GameClientState::getType,
			GameClientStateType::codec
	);

	GameClientStateType<?> getType();

	static void applyGlobally(GameClientState state, EventRegistrar events) {
		events.listen(GamePlayerEvents.ADD, player -> sendToPlayer(state, player));
		events.listen(GamePlayerEvents.REMOVE, player -> removeFromPlayer(state.getType(), player));
	}

	static <T extends GameClientState> void applyGlobally(IGamePhase game, EventRegistrar events, int interval, GameClientStateType<T> type, Function<ServerPlayer, T> generator) {
		Map<UUID, T> statesByPlayer = new Reference2ObjectOpenHashMap<>();
		events.listen(GamePlayerEvents.ADD, player -> {
			T state = generator.apply(player);
			statesByPlayer.put(player.getUUID(), state);
			sendToPlayer(state, player);
		});

		events.listen(GamePlayerEvents.REMOVE, player -> {
			if (statesByPlayer.remove(player.getUUID()) != null) {
				removeFromPlayer(type, player);
			}
		});

		MutableInt timer = new MutableInt();
		events.listen(GamePhaseEvents.TICK, () -> {
			if (timer.getAndIncrement() % interval != 0) {
				return;
			}
			for (Map.Entry<UUID, T> entry : statesByPlayer.entrySet()) {
				ServerPlayer player = game.allPlayers().getPlayerBy(entry.getKey());
				if (player == null) {
					continue;
				}
				T oldState = entry.getValue();
				T newState = generator.apply(player);
				if (!newState.equals(oldState)) {
					sendToPlayer(newState, player);
					entry.setValue(newState);
				}
			}
		});
	}

	static void sendToPlayer(GameClientState state, ServerPlayer player) {
		GameClientStateSender.get().byPlayer(player).enqueueSet(state);
	}

	static void sendToPlayers(GameClientState state, PlayerSet players) {
		for (ServerPlayer player : players) {
			sendToPlayer(state, player);
		}
	}

	static void removeFromPlayer(GameClientStateType<?> type, ServerPlayer player) {
		GameClientStateSender.get().byPlayer(player).enqueueRemove(type);
	}

	static void removeFromPlayers(GameClientStateType<?> type, PlayerSet players) {
		for (ServerPlayer player : players) {
			removeFromPlayer(type, player);
		}
	}
}
