package com.lovetropics.minigames.client.map;

import com.lovetropics.minigames.common.map.workspace.ClientWorkspaceRegions;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public final class RegionTraceTarget {
	public final ClientWorkspaceRegions.Entry entry;
	public final Direction side;
	public final Vector3d intersectPoint;
	public final double distanceToSide;

	public RegionTraceTarget(ClientWorkspaceRegions.Entry entry, Direction side, Vector3d intersectPoint, double distanceToSide) {
		this.entry = entry;
		this.side = side;
		this.intersectPoint = intersectPoint;
		this.distanceToSide = distanceToSide;
	}
}
