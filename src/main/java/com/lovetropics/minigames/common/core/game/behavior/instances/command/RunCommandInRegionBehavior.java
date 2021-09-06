package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
	protected void registerEvents(EventRegistrar events) {
		events.listen(GamePlayerEvents.TICK, (game, player) -> {
			if (player.ticksExisted % interval != 0) {
				return;
			}

			MapRegions regions = game.getMapRegions();
			for (String regionKey : commands.keySet()) {
				for (BlockBox region : regions.get(regionKey)) {
					if (region.contains(player.getPosX(), player.getPosY(), player.getPosZ())) {
						invoke(regionKey, sourceForEntity(player));
					}
				}
			}
		});
	}
}
