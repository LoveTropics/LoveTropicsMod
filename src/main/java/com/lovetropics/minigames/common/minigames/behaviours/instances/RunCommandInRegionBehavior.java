package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.List;
import java.util.Map;

public final class RunCommandInRegionBehavior extends CommandInvokeBehavior {
	private final int interval;

	public RunCommandInRegionBehavior(Map<String, List<String>> commands, int interval) {
		super(commands);
		this.interval = interval;
	}

	public static <T> RunCommandInRegionBehavior parse(Dynamic<T> root) {
		Map<String, List<String>> commands = parseCommands(root.get("regions").orElseEmptyMap());
		int interval = root.get("interval").asInt(20);

		return new RunCommandInRegionBehavior(commands, interval);
	}

	@Override
	public void onParticipantUpdate(IMinigameInstance minigame, ServerPlayerEntity player) {
		if (player.ticksExisted % interval != 0) {
			return;
		}

		MapRegions regions = minigame.getMapRegions();
		for (String regionKey : commands.keySet()) {
			for (MapRegion region : regions.get(regionKey)) {
				if (region.contains(player.getPosX(), player.getPosY(), player.getPosZ())) {
					invoke(regionKey, sourceForEntity(player));
				}
			}
		}
	}
}
