package com.lovetropics.minigames.client.lobby.select_role;

import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class SelectRoleMessage {
	private final boolean play;

	public SelectRoleMessage(boolean play) {
		this.play = play;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeBoolean(this.play);
	}

	public static SelectRoleMessage decode(PacketBuffer buffer) {
		return new SelectRoleMessage(buffer.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity player = ctx.get().getSender();
			IGameLobby lobby = IGameManager.get().getLobbyFor(player);
			if (lobby != null) {
				// TODO: handle
//				lobby.getPlayers().changeRole(player, )
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
