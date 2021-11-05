package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class JoinedLobbyMessage {
	private final int id;

	private JoinedLobbyMessage(int id) {
		this.id = id;
	}

	public static JoinedLobbyMessage create(IGameLobby lobby) {
		return new JoinedLobbyMessage(lobby.getMetadata().id().networkId());
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(id);
	}

	public static JoinedLobbyMessage decode(PacketBuffer buffer) {
		int instanceId = buffer.readVarInt();
		return new JoinedLobbyMessage(instanceId);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientLobbyManager.setJoined(id);
		});
		ctx.get().setPacketHandled(true);
	}
}
