package com.lovetropics.minigames.client.map;

import com.lovetropics.minigames.common.core.map.workspace.ClientWorkspaceRegions;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public record RegionTraceTarget(ClientWorkspaceRegions.Entry entry, Direction side, Vec3 intersectPoint, double distanceToSide) {
}
