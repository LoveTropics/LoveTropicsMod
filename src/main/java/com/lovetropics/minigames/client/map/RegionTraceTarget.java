package com.lovetropics.minigames.client.map;

import com.lovetropics.minigames.common.core.map.workspace.ClientWorkspaceRegions;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class RegionTraceTarget {
	public final ClientWorkspaceRegions.Entry entry;
	public final Direction side;
	public final Vec3 intersectPoint;
	public final double distanceToSide;

	public RegionTraceTarget(ClientWorkspaceRegions.Entry entry, Direction side, Vec3 intersectPoint, double distanceToSide) {
		this.entry = entry;
		this.side = side;
		this.intersectPoint = intersectPoint;
		this.distanceToSide = distanceToSide;
	}
}
