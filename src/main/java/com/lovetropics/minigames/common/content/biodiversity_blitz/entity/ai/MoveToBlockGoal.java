package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.EnumSet;

public abstract class MoveToBlockGoal extends Goal {
    protected final MobEntity mob;
    // State
    protected BlockPos targetPos;

    protected MoveToBlockGoal(MobEntity mob) {
        this.mob = mob;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        BlockPos target = locateBlock(12, 2);
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
