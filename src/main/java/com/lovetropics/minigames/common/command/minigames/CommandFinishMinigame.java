package com.lovetropics.minigames.common.command.minigames;

import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import static net.minecraft.command.Commands.literal;

public class CommandFinishMinigame {
	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
			literal("minigame")
			.then(literal("finish").requires(s -> s.getEntity() == null)
			.executes(c -> CommandMinigame.executeMinigameAction(() ->
				MinigameManager.getInstance().stop(), c.getSource())))
		);
	}
}
