package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.instances.control.ControlCommandInvoker;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;

import javax.annotation.Nullable;

// TODO: compose to reduce size of this interface
public interface IGameLobby {
	MinecraftServer getServer();

	GameLobbyMetadata getMetadata();

	PlayerSet getAllPlayers();

	LobbyGameQueue getGameQueue();

	@Nullable
	IActiveGame getActiveGame();

	ControlCommandInvoker getControlCommands();

	boolean registerPlayer(ServerPlayerEntity player, @Nullable PlayerRole requestedRole);

	boolean removePlayer(ServerPlayerEntity player);

	GameResult<Unit> requestStart();

	default boolean isVisibleTo(CommandSource source) {
		return true;
	}
}
