package com.lovetropics.minigames.common.command.minigames;

import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import static net.minecraft.command.Commands.literal;

public class CommandStopPollingMinigame {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("minigame")
			.then(literal("stop_poll").requires(s -> s.hasPermissionLevel(4))
			.executes(c -> CommandMinigame.executeMinigameAction(() ->
				MinigameManager.getInstance().stopPolling(), c.getSource())))
		);
	}
}
