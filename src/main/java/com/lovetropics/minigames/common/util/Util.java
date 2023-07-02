package com.lovetropics.minigames.common.util;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class Util {
    public static boolean spawnEntity(EntityType<?> entityType, Level world, double x, double y, double z) {
        if (entityType == EntityType.LIGHTNING_BOLT) {
            LightningBolt entity = EntityType.LIGHTNING_BOLT.create(world);
            entity.moveTo(new Vec3(x, y, z));
            world.addFreshEntity(entity);
            return true;
        } else {
            final Entity entity = entityType.create(world);
            if (entity != null) {
                entity.setPos(x, y, z);
                return world.addFreshEntity(entity);
            }

            return false;
        }
    }

    public static boolean addItemStackToInventory(final ServerPlayer player, final ItemStack itemstack) {
        if (player.addItem(itemstack)) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            return true;
        } else {
            ItemEntity item = player.drop(itemstack, false);
            if (item != null) {
                item.setNoPickUpDelay();
                item.setThrower(player.getUUID());
            }
        }

        return false;
    }

    public static ResourceLocation resource(String location) {
        return new ResourceLocation(Constants.MODID, location);
    }

    @Nullable
    public static BlockPos findGround(Level world, BlockPos origin, int maximumDistance) {
        if (!isSolidGround(world, origin)) {
            BlockPos pos = origin.below();
            if (world.getBlockState(pos).isSolid()) {
                return origin;
            }
        }

        // if this position is not free, scan upwards to find the ground
        if (world.getBlockState(origin).isSolid()) {
            BlockPos.MutableBlockPos mutablePos = origin.mutable();

            for (int i = 0; i < maximumDistance; i++) {
                mutablePos.move(Direction.UP);
                if (world.isOutsideBuildHeight(mutablePos) || !world.getBlockState(mutablePos).isSolid()) {
                    return mutablePos.immutable();
                }
            }
        }

        // if the position below us is not solid, scan downwards to find the ground
        BlockPos pos = origin.below();
        if (!world.getBlockState(pos).isSolid()) {
            BlockPos.MutableBlockPos mutablePos = origin.mutable();

            for (int i = 0; i < maximumDistance; i++) {
                mutablePos.move(Direction.DOWN);
                if (world.isOutsideBuildHeight(mutablePos)) {
                    return null;
                }

                if (world.getBlockState(mutablePos).isSolid()) {
                    return mutablePos.move(Direction.UP).immutable();
                }
            }
        }

        return null;
    }

    private static boolean isSolidGround(Level world, BlockPos pos) {
        return world.getBlockState(pos).isSolid();
    }

    public static void drawParticleBetween(ParticleOptions data, Vec3 start, Vec3 end, ServerLevel world, RandomSource random, int count, double xzScale, double yScale, double speedBase, double speedScale) {
        for (int i = 0; i < count; i++) {
            Vec3 sample = lerpVector(start, end, i / 20.0);
            double d3 = random.nextGaussian() * xzScale;
            double d1 = random.nextGaussian() * yScale;
            double d2 = random.nextGaussian() * xzScale;
            world.sendParticles(data, sample.x, sample.y, sample.z, 1 + random.nextInt(2), d3, d1, d2, speedBase + random.nextDouble() * speedScale);
        }
    }

    public static Vec3 lerpVector(Vec3 start, Vec3 end, double d) {
        return new Vec3(Mth.lerp(d, start.x, end.x), Mth.lerp(d, start.y, end.y), Mth.lerp(d, start.z, end.z));
    }

    public static Direction getDirectionBetween(BlockBox from, BlockBox to) {
        BlockPos fromCenter = from.centerBlock();
        BlockPos toCenter = to.centerBlock();
        int dx = toCenter.getX() - fromCenter.getX();
        int dz = toCenter.getZ() - fromCenter.getZ();
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }

    public static String formatMinutesSeconds(long totalSeconds) {
        long minutesPart = totalSeconds / 60;
        long secondsPart = totalSeconds % 60;
        return String.format("%02d:%02d", minutesPart, secondsPart);
    }
}
