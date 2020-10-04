package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.Dynamic;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public abstract class CommandInvokeBehavior implements IMinigameBehavior {
	private static final Logger LOGGER = LogManager.getLogger(CommandInvokeBehavior.class);

	protected final Map<String, List<String>> commands;

	private CommandDispatcher<CommandSource> dispatcher;
	private CommandSource source;

	public CommandInvokeBehavior(Map<String, List<String>> commands) {
		this.commands = commands;
	}

	public static <T> Map<String, List<String>> parseCommands(Dynamic<T> root) {
		return root.asMap(
				key -> key.asString(""),
				value -> value.asListOpt(CommandInvokeBehavior::parseCommand)
						.orElseGet(() -> ImmutableList.of(parseCommand(value)))
		);
	}

	private static <T> String parseCommand(Dynamic<T> value) {
		String command = value.asString("");
		if (command.startsWith("/")) {
			command = command.substring(1);
		}
		return command;
	}

	public void invoke(String key) {
		this.invoke(key, this.source);
	}

	public void invoke(String key, CommandSource source) {
		List<String> commands = this.commands.get(key);
		if (commands == null || commands.isEmpty()) {
			return;
		}

		for (String command : commands) {
			try {
				this.dispatcher.execute(command, source);
			} catch (CommandSyntaxException e) {
				LOGGER.error("Failed to execute command `{}` for {}", command, key, e);
			}
		}
	}

	public CommandSource sourceForEntity(Entity entity) {
		return this.source.withEntity(entity).withPos(entity.getPositionVec());
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		this.dispatcher = minigame.getServer().getCommandManager().getDispatcher();
	}

	@Override
	public void onStart(IMinigameInstance minigame) {
		this.source = minigame.getCommandSource();
	}
}
