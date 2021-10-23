package com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.world.IBlockReader;

public final class MpGroundNavigator extends GroundPathNavigator {
	private final MpMobEntity mob;

	public MpGroundNavigator(MobEntity mob) {
		super(mob, mob.world);
		this.mob = (MpMobEntity) mob;
	}

	@Override
	protected PathFinder getPathFinder(int maxDepth) {
		this.nodeProcessor = new NodeProcessor();
		this.nodeProcessor.setCanEnterDoors(true);
		return new PathFinder(this.nodeProcessor, maxDepth);
	}

	final class NodeProcessor extends WalkNodeProcessor {
		@Override
		public PathNodeType getFloorNodeType(IBlockReader world, int x, int y, int z) {
			if (!mob.getPlotWalls().getBounds().contains(x + 0.5, y + 0.5, z + 0.5)) {
				return PathNodeType.BLOCKED;
			}

			PathNodeType nodeType = super.getFloorNodeType(world, x, y, z);
			if (nodeType.getPriority() >= 0.0F && mob.getScareManager().isScaredAt(x, y, z)) {
				return PathNodeType.BLOCKED;
			}

			return nodeType;
		}
	}
}
