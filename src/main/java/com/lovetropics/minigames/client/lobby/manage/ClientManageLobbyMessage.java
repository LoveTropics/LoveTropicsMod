package com.lovetropics.minigames.client.lobby.manage;

import com.lovetropics.minigames.client.lobby.manage.state.update.ClientLobbyUpdate;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class ClientManageLobbyMessage {
	private final int id;
	private final ClientLobbyUpdate.Set updates;

	public ClientManageLobbyMessage(int id, ClientLobbyUpdate.Set updates) {
		this.id = id;
		this.updates = updates;
	}

	public static ClientManageLobbyMessage open(IGameLobby lobby) {
		return new ClientManageLobbyMessage(lobby.getMetadata().id().networkId(), ClientLobbyUpdate.Set.initialize(lobby));
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(id);
		updates.encode(buffer);
	}

	public static ClientManageLobbyMessage decode(PacketBuffer buffer) {
		int id = buffer.readVarInt();
		ClientLobbyUpdate.Set updates = ClientLobbyUpdate.Set.decode(buffer);
		return new ClientManageLobbyMessage(id, updates);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientLobbyManagement.update(id, updates);
		});
		ctx.get().setPacketHandled(true);
	}
}
