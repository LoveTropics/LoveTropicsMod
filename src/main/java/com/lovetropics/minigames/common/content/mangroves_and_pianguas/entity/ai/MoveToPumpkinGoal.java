package com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity.ai;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.CreatureEntity;

public final class MoveToPumpkinGoal extends MoveToBlockGoal {
    public MoveToPumpkinGoal(CreatureEntity creature) {
        super(creature);
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() == Blocks.PUMPKIN || state.getBlock() == Blocks.CARVED_PUMPKIN;
    }
}
