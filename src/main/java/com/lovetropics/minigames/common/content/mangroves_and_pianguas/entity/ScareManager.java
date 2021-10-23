package com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class ScareManager {
	private final List<BlockPos> scarySources = new ArrayList<>();
	private final int scareRadius2;

	public ScareManager(int scareRadius) {
		this.scareRadius2 = scareRadius * scareRadius;
	}

	public void addSource(BlockPos pos) {
		if (!scarySources.contains(pos)) {
			scarySources.add(pos);
		}
	}

	public boolean isScaredAt(int x, int y, int z) {
		for (BlockPos source : scarySources) {
			int dx = source.getX() - x;
			int dy = source.getY() - y;
			int dz = source.getZ() - z;
			int distance2 = dx * dx + dy * dy + dz * dz;
			if (distance2 <= scareRadius2) {
				return true;
			}
		}
		return false;
	}
}
