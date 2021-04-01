package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.polling.PollingGameInstance;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.Commands.literal;

public class StartGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("start").requires(s -> s.hasPermissionLevel(2))
			.executes(c -> {
				PollingGameInstance polling = IGameManager.get().getPollingGame();
				if (polling == null) {
					throw new SimpleCommandExceptionType(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME_POLLING)).create();
				}

				CompletableFuture<GameResult<ITextComponent>> future;
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
						source.sendFeedback(result.getOk(), false);
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
