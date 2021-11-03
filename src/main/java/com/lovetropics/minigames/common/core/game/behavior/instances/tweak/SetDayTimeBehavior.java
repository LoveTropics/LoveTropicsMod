package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class SetDayTimeBehavior implements IGameBehavior {
	public static final Codec<SetDayTimeBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.LONG.fieldOf("time").forGetter(c -> c.time)
		).apply(instance, SetDayTimeBehavior::new);
	});

	private final long time;

	public SetDayTimeBehavior(long time) {
		this.time = time;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		game.getWorld().setDayTime(this.time);
	}
}
