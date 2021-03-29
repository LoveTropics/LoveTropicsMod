package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.game.GameManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import static net.minecraft.command.Commands.literal;

public class StopPollingGameCommand {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("game")
			.then(literal("stop_poll").requires(s -> s.hasPermissionLevel(2))
			.executes(c -> GameCommand.executeMinigameAction(() ->
				GameManager.getInstance().stopPolling(), c.getSource())))
		);
	}
}
