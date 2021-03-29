package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.minigames.common.core.game.ControlCommand;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.GameControllable;
import com.lovetropics.minigames.common.core.game.behavior.IPollingMinigameBehavior;
import com.lovetropics.minigames.common.core.game.polling.PollingGameInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.List;
import java.util.Map;

public final class BindControlsBehavior extends CommandInvokeBehavior implements IPollingMinigameBehavior {
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
	public void onStartPolling(PollingGameInstance minigame) {
		super.onStartPolling(minigame);
		addControlsTo(minigame);
	}

	@Override
	public void onConstruct(IGameInstance minigame) {
		super.onConstruct(minigame);
		addControlsTo(minigame);
	}

	public void addControlsTo(GameControllable minigame) {
		for (Map.Entry<ControlCommand.Scope, Map<String, List<String>>> entry : scopedCommands.entrySet()) {
			ControlCommand.Scope scope = entry.getKey();
			Map<String, List<String>> commands = entry.getValue();

			for (String control : commands.keySet()) {
				minigame.addControlCommand(control, new ControlCommand(scope, source -> invoke(control, source)));
			}
		}
	}
}