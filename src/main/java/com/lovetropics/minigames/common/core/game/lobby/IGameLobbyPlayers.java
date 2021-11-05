package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerRoleSelections;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Unit;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public interface IGameLobbyPlayers extends PlayerSet {
	TeamAllocator<PlayerRole, ServerPlayerEntity> createRoleAllocator();

	CompletableFuture<GameResult<Unit>> join(ServerPlayerEntity player);

	boolean remove(ServerPlayerEntity player);

	boolean forceRole(ServerPlayerEntity player, @Nullable PlayerRole role);

	@Nullable
	PlayerRole getForcedRoleFor(ServerPlayerEntity player);

	PlayerRoleSelections getRoleSelections();
}
