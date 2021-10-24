package com.lovetropics.minigames.common.core.game.client_tweak;

import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.SetGameClientTweakMessage;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

public interface GameClientTweak {
	Codec<GameClientTweak> CODEC = GameClientTweakTypes.TYPE_CODEC.dispatch(
			"type",
			GameClientTweak::getType,
			GameClientTweakType::getCodec
	);

	GameClientTweakType<?> getType();

	default void applyGloballyTo(EventRegistrar events) {
		events.listen(GamePlayerEvents.ADD, player -> sendToPlayer(this, player));
		events.listen(GamePlayerEvents.REMOVE, player -> removeFromPlayer(getType(), player));
	}

	static void sendToPlayer(GameClientTweak tweak, ServerPlayerEntity player) {
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), SetGameClientTweakMessage.set(tweak));
	}

	static void removeFromPlayer(GameClientTweakType<?> type, ServerPlayerEntity player) {
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), SetGameClientTweakMessage.remove(type));
	}
}
