package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameControllable;
import com.lovetropics.minigames.common.minigames.behaviours.IPollingMinigameBehavior;
import com.lovetropics.minigames.common.minigames.polling.PollingMinigameInstance;
import com.mojang.datafixers.Dynamic;

import java.util.List;
import java.util.Map;

public final class BindControlsBehavior extends CommandInvokeBehavior implements IPollingMinigameBehavior {
	public BindControlsBehavior(Map<String, List<String>> commands) {
		super(commands);
	}

	public static <T> BindControlsBehavior parse(Dynamic<T> root) {
		Map<String, List<String>> commands = parseCommands(root.get("controls").orElseEmptyMap());
		return new BindControlsBehavior(commands);
	}

	@Override
	public void onStartPolling(PollingMinigameInstance minigame) {
		addControlsTo(minigame);
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		addControlsTo(minigame);
	}

	public void addControlsTo(MinigameControllable minigame) {
		for (String control : this.commands.keySet()) {
			minigame.addControlCommand(control, source -> invoke(control, source));
		}
	}
}
