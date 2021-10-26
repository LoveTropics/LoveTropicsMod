package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.state.control.ControlCommand;
import com.lovetropics.minigames.common.core.game.state.control.ControlCommands;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.List;
import java.util.Map;

public final class BindControlsBehavior extends CommandInvokeBehavior {
	public static final Codec<BindControlsBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.unboundedMap(ControlCommand.Scope.CODEC, COMMANDS_CODEC).fieldOf("controls").forGetter(c -> c.scopedCommands)
		).apply(instance, BindControlsBehavior::create);
	});

	private final Map<ControlCommand.Scope, Map<String, List<String>>> scopedCommands;

	private BindControlsBehavior(Map<ControlCommand.Scope, Map<String, List<String>>> scopedCommands, Map<String, List<String>> commands) {
		super(commands);
		this.scopedCommands = scopedCommands;
	}

	public static BindControlsBehavior create(Map<ControlCommand.Scope, Map<String, List<String>>> scopedCommands) {
		Map<String, List<String>> commands = new Object2ObjectOpenHashMap<>();
		for (Map<String, List<String>> scope : scopedCommands.values()) {
			commands.putAll(scope);
		}

		return new BindControlsBehavior(scopedCommands, commands);
	}

	@Override
	protected void registerEvents(IGamePhase game, EventRegistrar events) {
	}

	@Override
	protected void registerControls(IGamePhase game, ControlCommands controlCommands) {
		for (Map.Entry<ControlCommand.Scope, Map<String, List<String>>> entry : scopedCommands.entrySet()) {
			ControlCommand.Scope scope = entry.getKey();
			Map<String, List<String>> commands = entry.getValue();

			for (String control : commands.keySet()) {
				controlCommands.add(control, new ControlCommand(scope, source -> invoke(control, source)));
			}
		}
	}
}
