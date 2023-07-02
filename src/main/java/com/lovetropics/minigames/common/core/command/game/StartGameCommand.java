package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IGame;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.lobby.LobbyControls;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.lovetropics.minigames.common.util.Scheduler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.util.Unit;

import static net.minecraft.commands.Commands.literal;

public class StartGameCommand {
	private static final SimpleCommandExceptionType NOT_IN_LOBBY = new SimpleCommandExceptionType(GameTexts.Commands.notInLobby());
	private static final SimpleCommandExceptionType CANNOT_START_LOBBY = new SimpleCommandExceptionType(GameTexts.Commands.cannotStartLobby());

	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				literal("game")
						.then(literal("start").requires(s -> s.hasPermission(2))
								.executes(c -> {
									IGameLobby lobby = IGameManager.get().getLobbyFor(c.getSource());
									if (lobby == null) {
										throw NOT_IN_LOBBY.create();
									}

									LobbyControls.Action action = lobby.getControls().get(LobbyControls.Type.PLAY);
									if (action == null) {
										throw CANNOT_START_LOBBY.create();
									}

									Scheduler.nextTick().run(server -> {
										GameResult<Unit> result = action.run();
										if (result.isOk()) {
											IGame game = lobby.getCurrentGame();
											if (game != null) {
												c.getSource().sendSuccess(() -> GameTexts.Commands.startedGame(game.getDefinition()), false);
											}
										} else {
											c.getSource().sendFailure(result.getError());
										}
									});

									return Command.SINGLE_SUCCESS;
								}))
		);
	}
}
