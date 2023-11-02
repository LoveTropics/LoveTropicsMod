package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

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
        BlockPos target = locateBlock(31, 2);
        if (target != null) {
            this.targetPos = target;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ(), 0.5);
    }

    @Override
    public boolean canContinueToUse() {
        if (getBlockPriority(this.targetPos) > 0) {
            return false;
        }

        return !this.mob.getNavigation().isDone();
    }

    @Nullable
    protected BlockPos locateBlock(int rangeX, int rangeY) {
        BlockPos maxPrioLoc = null;
        int maxPrio = 0;
        BlockPos origin = this.mob.blockPosition();
        for (BlockPos pos : BlockPos.withinManhattan(origin, rangeX, rangeY, rangeX)) {
            pos = pos.immutable();
            int prio = getBlockPriority(pos);
            if (prio > maxPrio) {
                // If the found block has a higher priority, let's unconditionally go to it.
                maxPrio = prio;
                maxPrioLoc = pos;
            } else if (prio == maxPrio && maxPrioLoc != null && maxPrioLoc.distSqr(origin) > pos.distSqr(origin)) {
                // Same priority, but closer? Let's play fair and go to the closer one first.
                maxPrioLoc = pos;
            }
        }

        return maxPrioLoc;
    }

    // 0: Do not target this block
    // Anything higher will set the target to the current block, based on the current priority
    protected abstract int getBlockPriority(BlockPos pos);
}
