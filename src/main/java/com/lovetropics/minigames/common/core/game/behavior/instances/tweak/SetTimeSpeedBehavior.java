package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.instance.TimeInterpolationClientState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;

public record SetTimeSpeedBehavior(int factor) implements IGameBehavior {
	public static final MapCodec<SetTimeSpeedBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.INT.fieldOf("factor").forGetter(c -> c.factor)
	).apply(i, SetTimeSpeedBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePhaseEvents.TICK, () -> {
			ServerLevel world = game.getWorld();
			world.setDayTime(world.getDayTime() + this.factor - 1);
		});

		TimeInterpolationClientState state = new TimeInterpolationClientState(this.factor);
		GameClientState.applyGlobally(state, events);
	}
}
