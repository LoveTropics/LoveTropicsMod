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
		super(mob, mob.level);
		this.mob = (BbMobEntity) mob;
	}

	@Override
	protected PathFinder createPathFinder(int maxDepth) {
		this.nodeEvaluator = new NodeProcessor();
		this.nodeEvaluator.setCanPassDoors(true);
		return new PathFinder(this.nodeEvaluator, maxDepth);
	}

	final class NodeProcessor extends WalkNodeProcessor {
		@Override
		public PathNodeType getBlockPathType(IBlockReader world, int x, int y, int z) {
			BbMobBrain brain = BbGroundNavigator.this.mob.getMobBrain();

			if (!brain.getPlotWalls().getBounds().contains(x + 0.5, y + 0.5, z + 0.5)) {
				return PathNodeType.BLOCKED;
			}

			PathNodeType nodeType = super.getBlockPathType(world, x, y, z);
			if (nodeType.getMalus() >= 0.0F && brain.isScaredAt(x, y, z)) {
				return PathNodeType.BLOCKED;
			}

			return nodeType;
		}
	}
}
