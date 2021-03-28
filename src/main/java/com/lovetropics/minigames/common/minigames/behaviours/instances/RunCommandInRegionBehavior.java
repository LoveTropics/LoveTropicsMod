package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.List;
import java.util.Map;

public final class RunCommandInRegionBehavior extends CommandInvokeBehavior {
	public static final Codec<RunCommandInRegionBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				COMMANDS_CODEC.fieldOf("regions").forGetter(c -> c.commands),
				Codec.INT.optionalFieldOf("interval", 20).forGetter(c -> c.interval)
		).apply(instance, RunCommandInRegionBehavior::new);
	});

	private final int interval;

	public RunCommandInRegionBehavior(Map<String, List<String>> commands, int interval) {
		super(commands);
		this.interval = interval;
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
