package com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity.ai;

import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.EnumSet;

public abstract class MoveToBlockGoal extends Goal {
    private final MobEntity mob;
    // State
    private BlockPos targetPos;
    private int playerCheckTicks = 20;

    protected MoveToBlockGoal(MobEntity mob) {
        this.mob = mob;

        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean shouldExecute() {
        BlockPos target = locateBlock(this.mob.world, 12, 6);

        // If players are 3 blocks or closer to the mob then don't consider
        for (PlayerEntity player : this.mob.world.getPlayers()) {
            if (player.getDistance(this.mob) <= 3) {
                return false;
            }
        }

        if (target == null) {
            return false;
        }

        this.targetPos = target;

        return true;
    }

    @Override
    public void startExecuting() {
        this.playerCheckTicks = 20;

        this.mob.getNavigator().tryMoveToXYZ(this.targetPos.getX() + 0.25, this.targetPos.getY(), this.targetPos.getZ() + 0.25, 1.0);
    }

    @Override
    public void tick() {
        this.playerCheckTicks--;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.playerCheckTicks <= 0) {
            for (PlayerEntity player : this.mob.world.getPlayers()) {
                if (player.getDistance(this.mob) <= 5) {
                    return false;
                }
            }

            this.playerCheckTicks = 20;
        }

        return !this.mob.getNavigator().noPath();
    }

    @Nullable
    protected BlockPos locateBlock(IBlockReader world, int rangeX, int rangeY) {
        BlockPos blockPos = this.mob.getPosition();
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        int minimumDist = Integer.MAX_VALUE;
        BlockPos minPos = null;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(int dx = x - rangeX; dx <= x + rangeX; ++dx) {
            for(int dy = y - rangeY; dy <= y + rangeY; ++dy) {
                for(int dz = z - rangeX; dz <= z + rangeX; ++dz) {
                    mutable.setPos(dx, dy, dz);

                    if (isValidBlock(world.getBlockState(mutable))) {
                        int ax = dx - x;
                        int ay = dy - y;
                        int az = dz - z;

                        int dist = ax * ax + ay * ay + az * az;
                        if (dist < minimumDist) {
                            minimumDist = dist;
                            minPos = mutable.toImmutable();
                        }
                    }
                }
            }
        }

        return minPos;
    }

    protected abstract boolean isValidBlock(BlockState state);
}