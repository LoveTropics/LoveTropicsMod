package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.core.BlockPos;

public final class MoveToPumpkinGoal extends MoveToBlockGoal {
    public MoveToPumpkinGoal(PathfinderMob creature) {
        super(creature);
    }

    @Override
    protected int getBlockPriority(BlockPos pos) {
        BlockState state = mob.level.getBlockState(pos);
        return (state.getBlock() == Blocks.PUMPKIN || state.getBlock() == Blocks.CARVED_PUMPKIN) ? 100 : 0;
    }
}
