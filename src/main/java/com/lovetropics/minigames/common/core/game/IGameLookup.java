package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface IGameLookup {
	@Nullable
	IGameLobby getLobbyFor(PlayerEntity player);

	@Nullable
	IGameLobby getLobbyFor(Entity entity);

	@Nullable
	IGameLobby getLobbyAt(World world, BlockPos pos);

	@Nullable
	default IGameLobby getLobbyFor(CommandSource source) {
		Entity entity = source.getEntity();
		if (entity != null) {
			return getLobbyFor(entity);
		}
		return null;
	}

	@Nullable
	default IActiveGame getGameFor(PlayerEntity player) {
		IGameLobby lobby = getLobbyFor(player);
		return lobby != null ? lobby.getActiveGame() : null;
	}

	@Nullable
	default IActiveGame getGameFor(Entity entity) {
		IGameLobby lobby = getLobbyFor(entity);
		return lobby != null ? lobby.getActiveGame() : null;
	}

	@Nullable
	default IActiveGame getGameAt(World world, BlockPos pos) {
		IGameLobby lobby = getLobbyAt(world, pos);
		return lobby != null ? lobby.getActiveGame() : null;
	}

	@Nullable
	default IActiveGame getGameFor(CommandSource source) {
		IGameLobby lobby = getLobbyFor(source);
		return lobby != null ? lobby.getActiveGame() : null;
	}
}
