package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.state.instances.control.ControlCommandState;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
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
	public void registerWaiting(IGamePhase game, EventRegistrar events) throws GameException {
		MinecraftServer server = game.getServer();
		this.dispatcher = server.getCommandManager().getDispatcher();
		this.source = server.getCommandSource();

		this.registerControls(game, game.getState().get(ControlCommandState.TYPE));
	}

	@Override
	public void register(IActiveGame game, EventRegistrar events) {
		this.source = this.createCommandSource(game);

		this.registerControls(game, game.getState().get(ControlCommandState.TYPE));
		this.registerEvents(events);
	}

	private CommandSource createCommandSource(IActiveGame game) {
		String name = game.getLobby().getMetadata().name();
		return new CommandSource(ICommandSource.DUMMY, Vector3d.ZERO, Vector2f.ZERO, game.getWorld(), 4, name, new StringTextComponent(name), game.getServer(), null);
	}

	protected void registerControls(IGamePhase game, ControlCommandState commands) {
	}

	protected abstract void registerEvents(EventRegistrar events);
}
