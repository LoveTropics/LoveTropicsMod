package com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity.ai;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity.ScareableEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public final class ScaredGroundNavigator extends GroundPathNavigator {
	private final ScareableEntity scareable;

	public ScaredGroundNavigator(MobEntity mob, ScareableEntity scareable, World world) {
		super(mob, world);
		this.scareable = scareable;
	}

	@Override
	protected PathFinder getPathFinder(int maxDepth) {
		this.nodeProcessor = new ScaredWalkNodeProcessor();
		this.nodeProcessor.setCanEnterDoors(true);
		return new PathFinder(this.nodeProcessor, maxDepth);
	}

	final class ScaredWalkNodeProcessor extends WalkNodeProcessor {
		@Override
		public PathNodeType getFloorNodeType(IBlockReader world, int x, int y, int z) {
			PathNodeType nodeType = super.getFloorNodeType(world, x, y, z);
			if (nodeType.getPriority() >= 0.0F && scareable.getScareManager().isScaredAt(x, y, z)) {
				return PathNodeType.BLOCKED;
			}

			return nodeType;
		}
	}
}
