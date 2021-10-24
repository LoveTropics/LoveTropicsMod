package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.world.IBlockReader;

public final class BbGroundNavigator extends GroundPathNavigator {
	private final BbMobEntity mob;

	public BbGroundNavigator(MobEntity mob) {
		super(mob, mob.world);
		this.mob = (BbMobEntity) mob;
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
			BbMobBrain brain = mob.getMobBrain();

			if (!brain.getPlotWalls().getBounds().contains(x + 0.5, y + 0.5, z + 0.5)) {
				return PathNodeType.BLOCKED;
			}

			PathNodeType nodeType = super.getFloorNodeType(world, x, y, z);
			if (nodeType.getPriority() >= 0.0F && brain.isScaredAt(x, y, z)) {
				return PathNodeType.BLOCKED;
			}

			return nodeType;
		}
	}
}
