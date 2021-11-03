package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.state.control.ControlCommands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	protected CommandDispatcher<CommandSource> dispatcher;
	protected CommandSource source;

	public void invokeCommand(String command) {
		this.invokeCommand(command, this.source);
	}

	public void invokeCommand(String command, CommandSource source) {
		try {
			this.dispatcher.execute(command, source);
		} catch (CommandSyntaxException e) {
			LOGGER.error("Failed to execute command `{}` for {}", command, e);
		}
	}

	public CommandSource sourceForEntity(Entity entity) {
		return this.source.withEntity(entity).withPos(entity.getPositionVec());
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.dispatcher = game.getServer().getCommandManager().getDispatcher();
		this.source = this.createCommandSource(game);

		this.registerControls(game, game.getControlCommands());
		this.registerEvents(game, events);
	}

	private CommandSource createCommandSource(IGamePhase game) {
		String name = game.getLobby().getMetadata().name();
		return new CommandSource(ICommandSource.DUMMY, Vector3d.ZERO, Vector2f.ZERO, game.getWorld(), 4, name, new StringTextComponent(name), game.getServer(), null);
	}

	protected void registerControls(IGamePhase game, ControlCommands commands) {
	}

	protected abstract void registerEvents(IGamePhase game, EventRegistrar events);
}
