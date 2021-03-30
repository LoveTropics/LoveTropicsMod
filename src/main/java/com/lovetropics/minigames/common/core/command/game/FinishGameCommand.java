package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.game.GameManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import static net.minecraft.command.Commands.literal;

public class FinishGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("finish").requires(s -> s.getEntity() == null)
			.executes(c -> GameCommand.executeMinigameAction(() ->
				GameManager.get().finish(), c.getSource())))
		);
	}
}
