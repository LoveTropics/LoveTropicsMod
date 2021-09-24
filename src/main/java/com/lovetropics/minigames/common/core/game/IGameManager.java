package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.impl.MultiGameManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.state.instances.control.ControlCommandInvoker;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Specification for a game manager. Used to register game definitions
 * as well as hold the currently running game instance if applicable.
 * <p>
 * Implementations get to define the logic for polling, starting, stopping and
 * registering for polling games. Each of these actions return an ActionResult,
 * which are fed into Minecraft Commands to send these messages back to players
 * which execute the commands.
 */
public interface IGameManager extends IGameLookup {
	static IGameManager get() {
		return MultiGameManager.INSTANCE;
	}

	GameResult<IGameLobby> createGameLobby(String name, ServerPlayerEntity initiator);

	Collection<? extends IGameLobby> getAllLobbies();

	default Stream<? extends IGameLobby> getVisibleLobbies(CommandSource source) {
		return getAllLobbies().stream()
				.filter(lobby -> lobby.isVisibleTo(source));
	}

	@Nullable
	IGameLobby getLobbyByCommandId(String id);

	ControlCommandInvoker getControlCommands(CommandSource source);
}
