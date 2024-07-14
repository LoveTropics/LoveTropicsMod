package com.lovetropics.minigames.common.content.biodiversity_blitz.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class BrambleBlock extends BushBlock {
    public static final MapCodec<BrambleBlock> CODEC = simpleCodec(BrambleBlock::new);

    public BrambleBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected MapCodec<BrambleBlock> codec() {
        return CODEC;
    }

    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (pEntity instanceof LivingEntity && pEntity.getType() != EntityType.PLAYER) {
            pEntity.makeStuckInBlock(pState, new Vec3(0.8F, 0.75D, 0.8F));
            if ((pEntity.xOld != pEntity.getX() || pEntity.zOld != pEntity.getZ())) {
                double d0 = Math.abs(pEntity.getX() - pEntity.xOld);
                double d1 = Math.abs(pEntity.getZ() - pEntity.zOld);
                if (d0 >= (double)0.003F || d1 >= (double)0.003F) {
                    pEntity.hurt(pLevel.damageSources().sweetBerryBush(), 1.0F);
                }
            }

        }
    }

    @Override
    public PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        return PathType.DANGER_OTHER;
    }
}
