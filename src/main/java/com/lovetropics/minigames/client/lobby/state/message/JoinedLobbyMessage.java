package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class JoinedLobbyMessage {
	private final int id;
	private final PlayerRole registeredRole;

	private JoinedLobbyMessage(int id, PlayerRole registeredRole) {
		this.id = id;
		this.registeredRole = registeredRole;
	}

	public static JoinedLobbyMessage create(IGameLobby lobby, PlayerRole registeredRole) {
		return new JoinedLobbyMessage(lobby.getMetadata().id().networkId(), registeredRole);
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(id);
		buffer.writeBoolean(registeredRole != null);
		if (registeredRole != null) {
			buffer.writeEnumValue(registeredRole);
		}
	}

	public static JoinedLobbyMessage decode(PacketBuffer buffer) {
		int instanceId = buffer.readVarInt();
		PlayerRole role = null;
		if (buffer.readBoolean()) {
			role = buffer.readEnumValue(PlayerRole.class);
		}
		return new JoinedLobbyMessage(instanceId, role);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientLobbyManager.setJoined(id, registeredRole);
		});
		ctx.get().setPacketHandled(true);
	}
}
