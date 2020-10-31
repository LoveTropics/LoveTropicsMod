package com.lovetropics.minigames.common.command.minigames;

import static net.minecraft.command.Commands.literal;

import java.util.concurrent.CompletableFuture;

import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.MinigameResult;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class CommandStartMinigame {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("minigame")
			.then(literal("start").requires(s -> s.hasPermissionLevel(2))
			.executes(c -> {
				CompletableFuture<MinigameResult<ITextComponent>> future;
				try {
					future = MinigameManager.getInstance().start();
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
