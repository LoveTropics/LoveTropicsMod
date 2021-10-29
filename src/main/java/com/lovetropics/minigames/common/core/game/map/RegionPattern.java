package com.lovetropics.minigames.common.core.game.map;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.mojang.serialization.Codec;

import javax.annotation.Nullable;
import java.util.Collection;

public final class RegionPattern {
	public static final Codec<RegionPattern> CODEC = Codec.STRING.xmap(RegionPattern::new, p -> p.pattern);

	private final String pattern;

	public RegionPattern(String pattern) {
		this.pattern = pattern;
	}

	public Collection<BlockBox> get(MapRegions regions, Object... args) {
		return regions.get(this.resolveKey(args));
	}

	public BlockBox getOrThrow(MapRegions regions, Object... args) {
		return regions.getOrThrow(this.resolveKey(args));
	}

	@Nullable
	public BlockBox getAny(MapRegions regions, Object... args) {
		return regions.getAny(this.resolveKey(args));
	}

	private String resolveKey(Object[] args) {
		return String.format(pattern, args);
	}
}
