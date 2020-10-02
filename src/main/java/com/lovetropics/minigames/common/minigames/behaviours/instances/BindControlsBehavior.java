package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.mojang.datafixers.Dynamic;

import java.util.List;
import java.util.Map;

public final class BindControlsBehavior extends CommandInvokeBehavior {
	public BindControlsBehavior(Map<String, List<String>> commands) {
		super(commands);
	}

	public static <T> BindControlsBehavior parse(Dynamic<T> root) {
		Map<String, List<String>> commands = parseCommands(root.get("controls").orElseEmptyMap());
		return new BindControlsBehavior(commands);
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		super.onConstruct(minigame);
		for (String control : this.commands.keySet()) {
			minigame.addControlCommand(control, source -> invoke(control, source));
		}
	}
}
