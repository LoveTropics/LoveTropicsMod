package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.lobby.LobbyControls;
import com.lovetropics.minigames.common.core.game.util.GameMessages;
import com.lovetropics.minigames.common.util.Scheduler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Unit;
import net.minecraft.util.text.TranslationTextComponent;

import static net.minecraft.command.Commands.literal;

public class StartGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("start").requires(s -> s.hasPermissionLevel(2))
			.executes(c -> {
				IGameLobby lobby = IGameManager.get().getLobbyFor(c.getSource());
				if (lobby == null) {
					throw new SimpleCommandExceptionType(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME_POLLING)).create();
				}

				LobbyControls.Action action = lobby.getControls().get(LobbyControls.Type.PLAY);
				if (action == null) {
					// TODO: message
					throw new SimpleCommandExceptionType(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME_POLLING)).create();
				}

				Scheduler.nextTick().run(server -> {
					GameResult<Unit> result = action.run();
					if (result.isOk()) {
						c.getSource().sendFeedback(GameMessages.forLobby(lobby).startSuccess(), false);
					} else {
						c.getSource().sendErrorMessage(result.getError());
					}
				});

				return Command.SINGLE_SUCCESS;
			}))
		);
	}
}
