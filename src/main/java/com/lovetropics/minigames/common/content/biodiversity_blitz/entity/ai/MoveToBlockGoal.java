package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

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
            if (player.getDistanceSq(this.mob) <= 3 * 3) {
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

        this.mob.getNavigator().tryMoveToXYZ(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ(), 1.0);
    }

    @Override
    public void tick() {
        this.playerCheckTicks--;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.playerCheckTicks <= 0) {
            for (PlayerEntity player : this.mob.world.getPlayers()) {
                if (player.getDistanceSq(this.mob) <= 5 * 5) {
                    return false;
                }
            }

            this.playerCheckTicks = 20;
        }

        return !this.mob.getNavigator().noPath();
    }

    @Nullable
    protected BlockPos locateBlock(IBlockReader world, int rangeX, int rangeY) {
        BlockPos origin = this.mob.getPosition();
        int minimumDist = Integer.MAX_VALUE;
        BlockPos minPos = null;

        for (BlockPos pos : BlockPos.getAllInBoxMutable(origin.add(-rangeX, -rangeY, -rangeX), origin.add(rangeX, rangeY, rangeX))) {
            if (isValidBlock(world.getBlockState(pos))) {
                int dx = pos.getX() - origin.getX();
                int dy = pos.getY() - origin.getY();
                int dz = pos.getZ() - origin.getZ();

                int dist = dx * dx + dy * dy + dz * dz;
                if (dist < minimumDist) {
                    minimumDist = dist;
                    minPos = pos.toImmutable();
                }
            }
        }

        return minPos;
    }

    protected abstract boolean isValidBlock(BlockState state);
}
