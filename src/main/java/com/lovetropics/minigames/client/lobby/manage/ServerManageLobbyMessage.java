package com.lovetropics.minigames.client.lobby.manage;

import com.lovetropics.minigames.client.lobby.manage.state.update.ServerLobbyUpdate;
import com.lovetropics.minigames.common.core.game.impl.MultiGameManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class ServerManageLobbyMessage {
	private final int id;
	private final ServerLobbyUpdate.Set updates;

	public ServerManageLobbyMessage(int id, ServerLobbyUpdate.Set updates) {
		this.id = id;
		this.updates = updates;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(id);
		updates.encode(buffer);
	}

	public static ServerManageLobbyMessage decode(PacketBuffer buffer) {
		int id = buffer.readVarInt();
		ServerLobbyUpdate.Set updates = ServerLobbyUpdate.Set.decode(buffer);
		return new ServerManageLobbyMessage(id, updates);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			IGameLobby lobby = MultiGameManager.INSTANCE.getLobbyByNetworkId(id);
			if (lobby == null) {
				return;
			}

			ServerPlayerEntity player = ctx.get().getSender();
			if (!lobby.getMetadata().initiator().matches(player)) {
				return;
			}

			updates.applyTo(lobby);
		});
		ctx.get().setPacketHandled(true);
	}
}
