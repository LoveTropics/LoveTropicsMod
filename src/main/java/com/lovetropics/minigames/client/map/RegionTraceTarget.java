package com.lovetropics.minigames.client.map;

import com.lovetropics.minigames.common.map.workspace.ClientWorkspaceRegions;
import net.minecraft.util.Direction;

public final class RegionTraceTarget {
	public final ClientWorkspaceRegions.Entry entry;
	public final Direction side;
	public final double distance;

	public RegionTraceTarget(ClientWorkspaceRegions.Entry entry, Direction side, double distance) {
		this.entry = entry;
		this.side = side;
		this.distance = distance;
	}
}
