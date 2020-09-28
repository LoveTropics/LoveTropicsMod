package com.lovetropics.minigames.common.command.minigames;

import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.Commands.literal;

public class CommandStartMinigame {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("minigame")
			.then(literal("start").requires(s -> s.hasPermissionLevel(2))
			.executes(c -> {
				CompletableFuture<ActionResult<ITextComponent>> future = MinigameManager.getInstance().start();
				future.handleAsync((result, throwable) -> {
					if (throwable != null) {
						c.getSource().sendErrorMessage(new StringTextComponent("An unexpected exception was thrown when starting the game!"));
						throwable.printStackTrace();

						return null;
					}

					CommandSource source = c.getSource();
					if (result != null) {
						if (result.getType() != ActionResultType.FAIL) {
							source.sendFeedback(result.getResult(), true);
						} else {
							source.sendErrorMessage(result.getResult());
						}
					}

					return null;
				}, c.getSource().getServer());

				return Command.SINGLE_SUCCESS;
			}))
		);
	}
}
