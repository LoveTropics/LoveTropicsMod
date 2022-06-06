package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Map;

public final class RunCommandInRegionBehavior extends CommandInvokeMapBehavior {
	public static final Codec<RunCommandInRegionBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			COMMANDS_CODEC.fieldOf("regions").forGetter(c -> c.commands),
			Codec.INT.optionalFieldOf("interval", 20).forGetter(c -> c.interval)
	).apply(i, RunCommandInRegionBehavior::new));

	private final int interval;

	public RunCommandInRegionBehavior(Map<String, List<String>> commands, int interval) {
		super(commands);
		this.interval = interval;
	}

	@Override
	protected void registerEvents(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.TICK, (player) -> {
			if (player.tickCount % interval != 0) {
				return;
			}

			MapRegions regions = game.getMapRegions();
			for (String regionKey : commands.keySet()) {
				for (BlockBox region : regions.get(regionKey)) {
					if (region.contains(player.getX(), player.getY(), player.getZ())) {
						invoke(regionKey, sourceForEntity(player));
					}
				}
			}
		});
	}
}
