package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.control.ControlCommandInvoker;
import com.lovetropics.minigames.common.core.game.impl.PollingGame;
import com.lovetropics.minigames.common.core.game.impl.MultiGameManager;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.Collection;

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

	/**
	 * Starts polling the given game, allowing for players to register to join.
	 *
	 * @param game The game to be polled
	 * @param initiator the player starting this game
	 * @return The result of the polling attempt.
	 */
	GameResult<PollingGame> startPolling(IGameDefinition game, ServerPlayerEntity initiator);

	ControlCommandInvoker getControlInvoker(CommandSource source);

	Collection<? extends IGameInstance> getAllGames();

	@Nullable
	IGameInstance getGameByCommandId(String id);
}
