package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.control.ControlCommandInvoker;
import com.lovetropics.minigames.common.core.game.polling.PollingGameInstance;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Unit;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

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
		return SingleGameManager.INSTANCE;
	}

	/**
	 * Starts polling the given game, allowing for players to register to join.
	 *
	 * @param game The game to be polled
	 * @param initiator the player starting this game
	 * @return The result of the polling attempt.
	 */
	GameResult<PollingGameInstance> startPolling(IGameDefinition game, ServerPlayerEntity initiator);

	/**
	 * Stops polling the given actively polling game instance.
	 *
	 * @return The result of stopping the polling of an actively polling game.
	 */
	GameResult<Unit> stopPolling(PollingGameInstance game);

	/**
	 * Starts the given actively polling game if it has at least the minimum amount of
	 * participants registered to the game, specified by the game definition.
	 *
	 * @return The result of the start attempt.
	 */
	CompletableFuture<GameResult<IGameInstance>> start(PollingGameInstance game);

	/**
	 * Finishes the actively running game, indicating that the game exited normally.
	 * Generally, this means that games will upload results such as statistics and report a winner to players.
	 */
	GameResult<Unit> finish(IGameInstance game);

	/**
	 * Cancels the actively running game, indicating that it did not exit normally.
	 * Generally, this means games will not upload results such as statistics or report a winner to players.
	 */
	GameResult<Unit> cancel(IGameInstance game);

	ControlCommandInvoker getControlInvoker(CommandSource source);

	Collection<ProtoGameInstance> getAllGames();

	@Nullable
	ProtoGameInstance getGameById(String id);

	void close();
}
