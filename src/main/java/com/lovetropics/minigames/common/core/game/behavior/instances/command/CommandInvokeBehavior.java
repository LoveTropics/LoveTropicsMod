package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.state.DebugModeState;
import com.lovetropics.minigames.common.core.game.state.control.ControlCommands;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.TextComponent;
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

	protected CommandDispatcher<CommandSourceStack> dispatcher;
	protected CommandSourceStack source;

	public void invokeCommand(String command) {
		this.invokeCommand(command, this.source);
	}

	public void invokeCommand(String command, CommandSourceStack source) {
		try {
			this.dispatcher.execute(command, source);
		} catch (CommandSyntaxException e) {
			LOGGER.error("Failed to execute command `{}` for {}", command, e);
		}
	}

	public CommandSourceStack sourceForEntity(Entity entity) {
		return this.source.withEntity(entity).withPosition(entity.position());
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.dispatcher = game.getServer().getCommands().getDispatcher();
		this.source = this.createCommandSource(game);

		this.registerControls(game, game.getControlCommands());
		this.registerEvents(game, events);
	}

	private CommandSourceStack createCommandSource(IGamePhase game) {
		boolean debugMode = game.getState().getOrNull(DebugModeState.KEY) != null;
		CommandSource source = debugMode ? game.getServer() : CommandSource.NULL;

		String name = game.getLobby().getMetadata().name();
		return new CommandSourceStack(source, Vec3.ZERO, Vec2.ZERO, game.getWorld(), 4, name, new TextComponent(name), game.getServer(), null);
	}

	protected void registerControls(IGamePhase game, ControlCommands commands) {
	}

	protected abstract void registerEvents(IGamePhase game, EventRegistrar events);
}
