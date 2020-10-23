package com.lovetropics.minigames.common.minigames;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;

import java.util.Set;

public interface MinigameControllable {
	/**
	 * Adds a command with a custom task that can be used through the /minigame command while this game is active
	 * @param name the command name to use
	 * @param task the task to run when the command is invoked
	 */
	void addControlCommand(String name, ControlCommandHandler task);

	void invokeControlCommand(String name, CommandSource source) throws CommandSyntaxException;

	Set<String> getControlCommands();
}
