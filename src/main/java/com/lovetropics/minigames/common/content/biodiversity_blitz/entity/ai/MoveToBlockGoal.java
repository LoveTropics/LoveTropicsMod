package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.EnumSet;

public abstract class MoveToBlockGoal extends Goal {
    protected final Mob mob;
    // State
    protected BlockPos targetPos;

    protected MoveToBlockGoal(Mob mob) {
        this.mob = mob;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        BlockPos target = locateBlock(15, 2);
        if (target != null) {
            this.targetPos = target;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ(), 1.0);
    }

    @Override
    public boolean canContinueToUse() {
        if (!shouldTargetBlock(this.targetPos)) {
            return false;
        }

        return !this.mob.getNavigation().isDone();
    }

    @Nullable
    protected BlockPos locateBlock(int rangeX, int rangeY) {
        BlockPos origin = this.mob.blockPosition();
        for (BlockPos pos : BlockPos.withinManhattan(origin, rangeX, rangeY, rangeX)) {
            if (shouldTargetBlock(pos)) {
                return pos;
            }
        }
        return null;
    }

    protected abstract boolean shouldTargetBlock(BlockPos pos);
}
