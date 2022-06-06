package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class ScheduledCommandsBehavior extends CommandInvokeBehavior {
	public static final Codec<ScheduledCommandsBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			COMMAND_CODEC.fieldOf("run").forGetter(c -> c.command),
			Codec.LONG.fieldOf("time").forGetter(c -> c.time)
	).apply(i, ScheduledCommandsBehavior::new));

	private final String command;
	private final long time;

	public ScheduledCommandsBehavior(String command, long time) {
		this.command = command;
		this.time = time;
	}

	@Override
	protected void registerEvents(IGamePhase game, EventRegistrar events) {
		events.listen(GamePhaseEvents.TICK, () -> {
			if (game.ticks() == this.time) {
				this.invokeCommand(this.command);
			}
		});
	}
}
