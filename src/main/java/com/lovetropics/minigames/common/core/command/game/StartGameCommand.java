package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.game.GameManager;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.Commands.literal;

public class StartGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("start").requires(s -> s.hasPermissionLevel(2))
			.executes(c -> {
				CompletableFuture<GameResult<ITextComponent>> future;
				try {
					future = GameManager.get().start();
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
