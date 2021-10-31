package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.EnumSet;

public abstract class MoveToBlockGoal extends Goal {
    protected final MobEntity mob;
    // State
    protected BlockPos targetPos;

    protected MoveToBlockGoal(MobEntity mob) {
        this.mob = mob;

        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    private boolean shouldPrioritiseAttackPlant(BlockPos pos) {
        LivingEntity target = this.mob.getAttackTarget();
        if (target != null && this.shouldCompareWithTargetDistance()) {
            double plantDistance2 = this.mob.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            double targetDistance2 = this.mob.getDistance(target);
            return plantDistance2 < targetDistance2;
        } else {
            return true;
        }
    }

    private int getBlockSearchRange() {
        LivingEntity attackTarget = this.mob.getAttackTarget();
        int maxRange = Integer.MAX_VALUE;
        if (attackTarget != null) {
            maxRange = MathHelper.floor(mob.getDistance(attackTarget));
        }

        return Math.min(12, maxRange);
    }

    @Override
    public boolean shouldExecute() {
        BlockPos target = locateBlock(this.getBlockSearchRange(), 2);
        if (target != null && shouldPrioritiseAttackPlant(targetPos)) {
            this.targetPos = target;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void startExecuting() {
        this.mob.getNavigator().tryMoveToXYZ(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ(), 1.0);
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (!shouldPrioritiseAttackPlant(this.targetPos)) {
            return false;
        }

        if (!shouldTargetBlock(this.targetPos)) {
            return false;
        }

        return !this.mob.getNavigator().noPath();
    }

    protected boolean shouldCompareWithTargetDistance() {
        return true;
    }

    @Nullable
    protected BlockPos locateBlock(int rangeX, int rangeY) {
        BlockPos origin = this.mob.getPosition();
        for (BlockPos pos : BlockPos.getProximitySortedBoxPositionsIterator(origin, rangeX, rangeY, rangeX)) {
            if (shouldTargetBlock(pos)) {
                return pos;
            }
        }
        return null;
    }

    protected abstract boolean shouldTargetBlock(BlockPos pos);
}
