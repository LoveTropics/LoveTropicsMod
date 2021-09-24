package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientJoinedLobbyMessage {
	private final int id;
	private final PlayerRole role;

	public ClientJoinedLobbyMessage(int id, PlayerRole role) {
		this.id = id;
		this.role = role;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(id);
		buffer.writeBoolean(role != null);
		if (role != null) {
			buffer.writeEnumValue(role);
		}
	}

	public static ClientJoinedLobbyMessage decode(PacketBuffer buffer) {
		int instanceId = buffer.readVarInt();
		PlayerRole role = null;
		if (buffer.readBoolean()) {
			role = buffer.readEnumValue(PlayerRole.class);
		}
		return new ClientJoinedLobbyMessage(instanceId, role);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientLobbyManager.setJoined(id, role);
		});
		ctx.get().setPacketHandled(true);
	}
}
