package com.lovetropics.minigames.client.lobby.select_role;

import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SelectRoleMessage(int lobbyId, boolean play) {
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeVarInt(this.lobbyId);
		buffer.writeBoolean(this.play);
	}

	public static SelectRoleMessage decode(FriendlyByteBuf buffer) {
		return new SelectRoleMessage(buffer.readVarInt(), buffer.readBoolean());
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		IGameLobby lobby = IGameManager.get().getLobbyByNetworkId(lobbyId);
		if (lobby != null) {
			PlayerRole role = play ? PlayerRole.PARTICIPANT : PlayerRole.SPECTATOR;
			lobby.getPlayers().getRoleSelections().acceptResponse(player, role);
		}
	}
}
