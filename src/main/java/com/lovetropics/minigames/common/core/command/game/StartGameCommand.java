package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.polling.PollingGameInstance;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.Commands.literal;

public class StartGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("start").requires(s -> s.hasPermissionLevel(2))
			.executes(c -> {
				// TODO: this is terrible
				Collection<ProtoGameInstance> games = IGameManager.get().getAllGames();
				if (games.size() != 1) {
					throw new SimpleCommandExceptionType(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME_POLLING)).create();
				}

				ProtoGameInstance game = games.iterator().next();
				if (!(game instanceof PollingGameInstance)) {
					throw new SimpleCommandExceptionType(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME_POLLING)).create();
				}

				PollingGameInstance polling = (PollingGameInstance) game;

				CompletableFuture<GameResult<IGameInstance>> future;
				try {
					future = IGameManager.get().start(polling);
				} catch (Exception e) {
					c.getSource().sendFeedback(new StringTextComponent("Unexpected error starting minigame: " + e), true);
					return 0;
				}
				future.handleAsync((result, throwable) -> {
					if (throwable != null) {
						c.getSource().sendErrorMessage(new StringTextComponent("An unexpected exception was thrown when starting the game!"));
						throwable.printStackTrace();

						return null;
					}

					CommandSource source = c.getSource();
					if (result.isOk()) {
						source.sendFeedback(GameMessages.forGame(game).startSuccess(), false);
					} else {
						source.sendErrorMessage(result.getError());
					}

					return null;
				}, c.getSource().getServer());

				return Command.SINGLE_SUCCESS;
			}))
		);
	}
}
