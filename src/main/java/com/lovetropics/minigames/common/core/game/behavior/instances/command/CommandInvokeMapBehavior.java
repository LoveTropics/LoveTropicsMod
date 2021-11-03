package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;

import java.util.List;
import java.util.Map;

public abstract class CommandInvokeMapBehavior extends CommandInvokeBehavior {
	public static final Codec<Map<String, List<String>>> COMMANDS_CODEC = Codec.unboundedMap(Codec.STRING, MoreCodecs.listOrUnit(COMMAND_CODEC));

	protected final Map<String, List<String>> commands;

	public CommandInvokeMapBehavior(Map<String, List<String>> commands) {
		this.commands = commands;
	}

	public void invoke(String key) {
		this.invoke(key, this.source);
	}

	public void invoke(String key, Entity source) {
		this.invoke(key, this.sourceForEntity(source));
	}

	public void invoke(String key, CommandSource source) {
		List<String> commands = this.commands.get(key);
		if (commands == null || commands.isEmpty()) {
			return;
		}

		for (String command : commands) {
			this.invokeCommand(command, source);
		}
	}
}
