package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class MoveToPumpkinGoal extends MoveToBlockGoal {
    public MoveToPumpkinGoal(PathfinderMob creature) {
        super(creature);
    }

    @Override
    protected int getBlockPriority(BlockPos pos) {
        BlockState state = mob.level().getBlockState(pos);
        return state.is(Blocks.PUMPKIN) || state.is(Blocks.CARVED_PUMPKIN) ? 100 : 0;
    }
}
