package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.game.IGameManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import static net.minecraft.command.Commands.literal;

public class FinishGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("finish").requires(s -> s.getEntity() == null)
			.executes(c -> GameCommand.executeMinigameAction(() ->
				IGameManager.get().finish(IGameManager.get().getActiveGame()), c.getSource())))
		);
	}
}
