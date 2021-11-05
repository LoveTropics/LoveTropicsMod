package com.lovetropics.minigames.client.lobby.manage.state;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;
import java.util.UUID;

public final class ClientLobbyPlayer {
	private final UUID uuid;
	@Nullable
	private final PlayerRole playingRole;

	private ClientLobbyPlayer(UUID uuid, @Nullable PlayerRole playingRole) {
		this.uuid = uuid;
		this.playingRole = playingRole;
	}

	public static ClientLobbyPlayer from(IGameLobby lobby, ServerPlayerEntity player) {
		IGamePhase currentPhase = lobby.getCurrentPhase();
		PlayerRole playingRole = currentPhase != null ? currentPhase.getRoleFor(player) : null;
		return new ClientLobbyPlayer(player.getUniqueID(), playingRole);
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeUniqueId(this.uuid);
		encodeRole(buffer, this.playingRole);
	}

	public static ClientLobbyPlayer decode(PacketBuffer buffer) {
		return new ClientLobbyPlayer(
				buffer.readUniqueId(),
				decodeRole(buffer)
		);
	}

	private static void encodeRole(PacketBuffer buffer, @Nullable PlayerRole role) {
		if (role != null) {
			buffer.writeVarInt(role.ordinal() + 1);
		} else {
			buffer.writeVarInt(0);
		}
	}

	@Nullable
	private static PlayerRole decodeRole(PacketBuffer buffer) {
		int ordinal = buffer.readVarInt() - 1;
		if (ordinal >= 0 && ordinal < PlayerRole.ROLES.length) {
			return PlayerRole.ROLES[ordinal];
		} else {
			return null;
		}
	}

	public UUID uuid() {
		return this.uuid;
	}

	@Nullable
	public PlayerRole playingRole() {
		return this.playingRole;
	}
}
