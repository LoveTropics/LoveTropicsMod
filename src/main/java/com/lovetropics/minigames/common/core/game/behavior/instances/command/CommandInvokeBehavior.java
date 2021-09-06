package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.IPollingGame;
import com.lovetropics.minigames.common.core.game.IProtoGame;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.control.GameControlCommands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class CommandInvokeBehavior implements IGameBehavior {
	private static final Logger LOGGER = LogManager.getLogger(CommandInvokeBehavior.class);

	public static final Codec<String> COMMAND_CODEC = Codec.STRING.xmap(
			command -> {
				if (command.startsWith("/")) {
					command = command.substring(1);
				}
				return command;
			},
			Function.identity()
	);

	public static final Codec<Map<String, List<String>>> COMMANDS_CODEC = Codec.unboundedMap(Codec.STRING, MoreCodecs.listOrUnit(COMMAND_CODEC));

	protected final Map<String, List<String>> commands;

	private CommandDispatcher<CommandSource> dispatcher;
	private CommandSource source;

	public CommandInvokeBehavior(Map<String, List<String>> commands) {
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
	public void registerPolling(IPollingGame game, EventRegistrar events) throws GameException {
		MinecraftServer server = game.getServer();
		this.dispatcher = server.getCommandManager().getDispatcher();
		this.source = server.getCommandSource();

		this.registerControls(game, game.getControlCommands());
	}

	@Override
	public void register(IActiveGame game, EventRegistrar events) {
		this.source = game.getCommandSource();
		this.registerControls(game, game.getControlCommands());
		this.registerEvents(events);
	}

	protected void registerControls(IProtoGame game, GameControlCommands commands) {
	}

	protected abstract void registerEvents(EventRegistrar events);
}
