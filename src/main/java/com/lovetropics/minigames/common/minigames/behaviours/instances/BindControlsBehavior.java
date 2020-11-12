package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.ControlCommand;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameControllable;
import com.lovetropics.minigames.common.minigames.behaviours.IPollingMinigameBehavior;
import com.lovetropics.minigames.common.minigames.polling.PollingMinigameInstance;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.List;
import java.util.Map;

public final class BindControlsBehavior extends CommandInvokeBehavior implements IPollingMinigameBehavior {
	private final Map<ControlCommand.Scope, Map<String, List<String>>> commands;

	private BindControlsBehavior(Map<ControlCommand.Scope, Map<String, List<String>>> scopedCommands, Map<String, List<String>> commands) {
		super(commands);
		this.commands = scopedCommands;
	}

	public static <T> BindControlsBehavior parse(Dynamic<T> root) {
		Map<ControlCommand.Scope, Map<String, List<String>>> scopedCommands = root.get("controls").asMap(
				key -> {
					ControlCommand.Scope scope = ControlCommand.Scope.byKey(key.asString(""));
					return scope != null ? scope : ControlCommand.Scope.ADMINS;
				},
				CommandInvokeBehavior::parseCommands
		);

		Map<String, List<String>> commands = new Object2ObjectOpenHashMap<>();
		for (Map<String, List<String>> scope : scopedCommands.values()) {
			commands.putAll(scope);
		}

		return new BindControlsBehavior(scopedCommands, commands);
	}

	@Override
	public void onStartPolling(PollingMinigameInstance minigame) {
		super.onStartPolling(minigame);
		addControlsTo(minigame);
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		super.onConstruct(minigame);
		addControlsTo(minigame);
	}

	public void addControlsTo(MinigameControllable minigame) {
		for (Map.Entry<ControlCommand.Scope, Map<String, List<String>>> entry : commands.entrySet()) {
			ControlCommand.Scope scope = entry.getKey();
			Map<String, List<String>> commands = entry.getValue();

			for (String control : commands.keySet()) {
				minigame.addControlCommand(control, new ControlCommand(scope, source -> invoke(control, source)));
			}
		}
	}
}
