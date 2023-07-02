package com.lovetropics.minigames.common.core.game.state.statistics;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum PlacementOrder implements StringRepresentable {
	MAX("max"),
	MIN("min");

	public static final Codec<PlacementOrder> CODEC = StringRepresentable.fromEnum(PlacementOrder::values);

	private final String key;

	PlacementOrder(String key) {
		this.key = key;
	}

	@Override
	public String getSerializedName() {
		return key;
	}
}
