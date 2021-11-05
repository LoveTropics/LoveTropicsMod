package com.lovetropics.minigames.client.lobby.select_role;

import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public final class SelectRoleMessage {
	private final int lobbyId;
	private final boolean play;

	public SelectRoleMessage(int lobbyId, boolean play) {
		this.lobbyId = lobbyId;
		this.play = play;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(this.lobbyId);
		buffer.writeBoolean(this.play);
	}

	public static SelectRoleMessage decode(PacketBuffer buffer) {
		return new SelectRoleMessage(buffer.readVarInt(), buffer.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity player = ctx.get().getSender();
			IGameLobby lobby = IGameManager.get().getLobbyByNetworkId(lobbyId);
			if (lobby != null) {
				PlayerRole role = play ? PlayerRole.PARTICIPANT : PlayerRole.SPECTATOR;
				lobby.getPlayers().getRoleSelections().acceptResponse(player, role);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
