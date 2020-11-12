package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface MinigameControllable {
	/**
	 * Adds a command with a custom task that can be used through the /minigame command while this game is active
	 * @param name the command name to use
	 * @param command the control command
	 */
	void addControlCommand(String name, ControlCommand command);

	void invokeControlCommand(String name, CommandSource source) throws CommandSyntaxException;

	Stream<String> controlCommandsFor(CommandSource source);

	@Nullable
	PlayerKey getInitiator();
}
