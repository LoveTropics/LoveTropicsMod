package com.lovetropics.minigames.common.core.game.control;

import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.command.CommandSource;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Stream;

public final class GameControlCommands implements ControlCommandInvoker {
	private final PlayerKey initiator;
	private final Map<String, ControlCommand> controlCommands = new Object2ObjectOpenHashMap<>();

	public GameControlCommands(@Nullable PlayerKey initiator) {
		this.initiator = initiator;
	}

	public void add(String name, ControlCommand command) {
		this.controlCommands.put(name, command);
	}

	@Override
	public void invoke(String name, CommandSource source) throws CommandSyntaxException {
		ControlCommand command = this.controlCommands.get(name);
		if (command != null) {
			command.invoke(source, this.initiator);
		}
	}

	@Override
	public Stream<String> list(CommandSource source) {
		return this.controlCommands.entrySet().stream()
				.filter(entry -> entry.getValue().canUse(source, this.initiator))
				.map(Map.Entry::getKey);
	}
}
