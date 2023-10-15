package com.lovetropics.minigames.common.core.game.behavior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import net.minecraft.Util;

// Behavior instances are not reusable, so we need to reconstruct from data each time we need them
public class BehaviorTemplate {
	public static final Codec<BehaviorTemplate> CODEC = Codec.PASSTHROUGH.comapFlatMap(
			dynamic -> {
				BehaviorTemplate template = new BehaviorTemplate(dynamic);
				return IGameBehavior.CODEC.parse(dynamic).map(behavior -> template);
			},
			template -> template.data
	);

	private final Dynamic<?> data;

	private BehaviorTemplate(Dynamic<?> data) {
		this.data = data;
	}

	public IGameBehavior instantiate() {
		// Data has already been validated, something has gone wrong if we fail to parse again
		return IGameBehavior.CODEC.parse(data).resultOrPartial(s -> {}).orElseThrow();
	}
}
