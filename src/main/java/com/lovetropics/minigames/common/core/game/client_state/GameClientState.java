package com.lovetropics.minigames.common.core.game.client_state;

import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.SetGameClientStateMessage;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

public interface GameClientState {
	Codec<GameClientState> CODEC = GameClientStateTypes.TYPE_CODEC.dispatch(
			"type",
			GameClientState::getType,
			GameClientStateType::getCodec
	);

	GameClientStateType<?> getType();

	default void applyGloballyTo(EventRegistrar events) {
		events.listen(GamePlayerEvents.ADD, player -> sendToPlayer(this, player));
		events.listen(GamePlayerEvents.REMOVE, player -> removeFromPlayer(getType(), player));
	}

	static void sendToPlayer(GameClientState state, ServerPlayerEntity player) {
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), SetGameClientStateMessage.set(state));
	}

	static void sendToPlayers(GameClientState state, PlayerSet players) {
		players.sendPacket(LoveTropicsNetwork.CHANNEL, SetGameClientStateMessage.set(state));
	}

	static void removeFromPlayer(GameClientStateType<?> type, ServerPlayerEntity player) {
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), SetGameClientStateMessage.remove(type));
	}

	static void removeFromPlayers(GameClientStateType<?> type, PlayerSet players) {
		players.sendPacket(LoveTropicsNetwork.CHANNEL, SetGameClientStateMessage.remove(type));
	}
}
