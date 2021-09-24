package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class LeftLobbyMessage {
	public LeftLobbyMessage() {
	}

	public void encode(PacketBuffer buffer) {
	}

	public static LeftLobbyMessage decode(PacketBuffer buffer) {
		return new LeftLobbyMessage();
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(ClientLobbyManager::clearJoined);
		ctx.get().setPacketHandled(true);
	}
}
