package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweak;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class SetGameClientTweakBehavior implements IGameBehavior {
	public static final Codec<SetGameClientTweakBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				GameClientTweak.CODEC.fieldOf("tweak").forGetter(c -> c.tweak)
		).apply(instance, SetGameClientTweakBehavior::new);
	});

	private final GameClientTweak tweak;

	public SetGameClientTweakBehavior(GameClientTweak tweak) {
		this.tweak = tweak;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		game.getClientTweaks().add(tweak);
	}
}
