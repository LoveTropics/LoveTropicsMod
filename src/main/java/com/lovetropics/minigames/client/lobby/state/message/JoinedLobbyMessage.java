package com.lovetropics.minigames.client.lobby.state.message;

import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class JoinedLobbyMessage {
	private final int id;
	private final PlayerRole role;

	private JoinedLobbyMessage(int id, PlayerRole role) {
		this.id = id;
		this.role = role;
	}

	public static JoinedLobbyMessage create(IGameLobby lobby, PlayerRole role) {
		return new JoinedLobbyMessage(lobby.getMetadata().id().networkId(), role);
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(id);
		buffer.writeBoolean(role != null);
		if (role != null) {
			buffer.writeEnumValue(role);
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
			ClientLobbyManager.setJoined(id, role);
		});
		ctx.get().setPacketHandled(true);
	}
}
