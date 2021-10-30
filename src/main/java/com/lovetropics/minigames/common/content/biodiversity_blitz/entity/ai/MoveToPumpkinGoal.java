package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.util.math.BlockPos;

public final class MoveToPumpkinGoal extends MoveToBlockGoal {
    public MoveToPumpkinGoal(CreatureEntity creature) {
        super(creature);
    }

    @Override
    protected boolean isValidBlock(BlockPos pos, BlockState state) {
        return state.getBlock() == Blocks.PUMPKIN || state.getBlock() == Blocks.CARVED_PUMPKIN;
    }
}
