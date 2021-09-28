package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;

public interface IGameLobbyPlayers extends PlayerSet {
	TeamAllocator<PlayerRole, ServerPlayerEntity> createRoleAllocator();

	boolean register(ServerPlayerEntity player, @Nullable PlayerRole requestedRole);

	boolean remove(ServerPlayerEntity player);

	@Nullable
	PlayerRole getRegisteredRoleFor(ServerPlayerEntity player);

	default int getParticipantCount() {
		return getCountWithRole(PlayerRole.PARTICIPANT);
	}

	default int getSpectatorCount() {
		return getCountWithRole(PlayerRole.SPECTATOR);
	}

	default int getCountWithRole(PlayerRole role) {
		int count = 0;
		for (ServerPlayerEntity player : this) {
			if (getRegisteredRoleFor(player) == role) {
				count++;
			}
		}
		return count;
	}
}
