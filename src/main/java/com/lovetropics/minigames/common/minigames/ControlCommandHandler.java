package com.lovetropics.minigames.common.minigames;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;

public interface ControlCommandHandler {
	void run(CommandSource source) throws CommandSyntaxException;
}
