package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;

public enum PackagePlayerSelect {
	SPECIFIC("specific"), RANDOM("random"), ALL("all");

	public static final Codec<PackagePlayerSelect> CODEC = MoreCodecs.stringVariants(PackagePlayerSelect.values(), s -> s.type);

	public final String type;

	PackagePlayerSelect(final String type) {
		this.type = type;
	}
}
