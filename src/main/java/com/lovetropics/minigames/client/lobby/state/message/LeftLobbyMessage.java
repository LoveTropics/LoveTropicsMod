package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record LeftLobbyMessage() {
	public void encode(FriendlyByteBuf buffer) {
	}

	public static LeftLobbyMessage decode(FriendlyByteBuf buffer) {
		return new LeftLobbyMessage();
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ClientLobbyManager.clearJoined();
	}
}
