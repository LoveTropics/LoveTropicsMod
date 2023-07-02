package com.lovetropics.minigames.client.lobby.manage;

import com.lovetropics.minigames.client.lobby.manage.state.update.ClientLobbyUpdate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientManageLobbyMessage(int id, ClientLobbyUpdate.Set updates) {
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeVarInt(id);
		updates.encode(buffer);
	}

	public static ClientManageLobbyMessage decode(FriendlyByteBuf buffer) {
		int id = buffer.readVarInt();
		ClientLobbyUpdate.Set updates = ClientLobbyUpdate.Set.decode(buffer);
		return new ClientManageLobbyMessage(id, updates);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ClientLobbyManagement.update(id, updates);
	}
}
