package com.lovetropics.minigames.common.util;

import com.google.common.collect.Lists;
import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class Util {

    /*public static boolean removeTask(CreatureEntity ent, Class taskToReplace) {
        for (Goal entry : ent.goalSelector..taskEntries) {
            if (taskToReplace.isAssignableFrom(entry.action.getClass())) {
                ent.tasks.removeTask(entry.action);
                return true;
            }
        }

        return false;
    }*/

    public static boolean spawnEntity(EntityType<?> entityType, World world, double x, double y, double z) {
        if (entityType == EntityType.LIGHTNING_BOLT) {
            LightningBoltEntity entity = EntityType.LIGHTNING_BOLT.create(world);
            entity.moveTo(new Vector3d(x, y, z));
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

    public static boolean addItemStackToInventory(final ServerPlayerEntity player, final ItemStack itemstack) {
        if (player.addItem(itemstack)) {
            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            return true;
        } else {
            ItemEntity itementity = player.drop(itemstack, false);
            if (itementity != null) {
                itementity.setNoPickUpDelay();
                itementity.setOwner(player.getUUID());
            }
        }

        return false;
    }

    public static boolean tryMoveToEntityLivingLongDist(MobEntity entSource, Entity entityTo, double moveSpeedAmp) {
        return tryMoveToXYZLongDist(entSource, entityTo.blockPosition(), moveSpeedAmp);
    }

    public static boolean tryMoveToXYZLongDist(MobEntity ent, BlockPos pos, double moveSpeedAmp) {
        return tryMoveToXYZLongDist(ent, pos.getX(), pos.getY(), pos.getZ(), moveSpeedAmp);
    }

    /**
     * From CoroUtilPath
     * If close enough, paths to coords, if too far based on attribute, tries to find best spot towards target to pathfind to
     *
     * @param ent
     * @param x
     * @param y
     * @param z
     * @param moveSpeedAmp
     * @return
     */
    public static boolean tryMoveToXYZLongDist(MobEntity ent, int x, int y, int z, double moveSpeedAmp) {

        World world = ent.level;

        boolean success = false;

        if (ent.getNavigation().isDone()) {

            double distToPlayer = getDistance(ent, x, y, z);//ent.getDistanceToEntity(player);

            double followDist = ent.getAttribute(Attributes.FOLLOW_RANGE).getValue();

            if (distToPlayer <= followDist) {
                //boolean success = ent.getNavigator().tryMoveToEntityLiving(player, moveSpeedAmp);
                success = ent.getNavigation().moveTo(x, y, z, moveSpeedAmp);
                //System.out.println("success? " + success + "- move to player: " + ent + " -> " + player);
            } else {
		        /*int x = MathHelper.floor(player.posX);
		        int y = MathHelper.floor(player.posY);
		        int z = MathHelper.floor(player.posZ);*/

                double d = x+0.5F - ent.getX();
                double d2 = z+0.5F - ent.getZ();
                double d1;
                d1 = y+0.5F - (ent.getY() + (double)ent.getEyeHeight());

                double d3 = MathHelper.sqrt(d * d + d2 * d2);
                float f2 = (float)((Math.atan2(d2, d) * 180D) / 3.1415927410125732D) - 90F;
                float f3 = (float)(-((Math.atan2(d1, d3) * 180D) / 3.1415927410125732D));
                float rotationPitch = -f3;//-ent.updateRotation(rotationPitch, f3, 180D);
                float rotationYaw = f2;//updateRotation(rotationYaw, f2, 180D);

                LivingEntity center = ent;

                Random rand = world.random;

                float randLook = rand.nextInt(90)-45;
                //int height = 10;
                double dist = (followDist * 0.75D) + rand.nextInt((int)followDist / 2);//rand.nextInt(26)+(queue.get(0).retryState * 6);
                int gatherX = (int)Math.floor(center.getX() + ((double)(-Math.sin((rotationYaw+randLook) / 180.0F * 3.1415927F)/* * Math.cos(center.rotationPitch / 180.0F * 3.1415927F)*/) * dist));
                int gatherY = (int)center.getY();//Math.floor(center.posY-0.5 + (double)(-MathHelper.sin(center.rotationPitch / 180.0F * 3.1415927F) * dist) - 0D); //center.posY - 0D;
                int gatherZ = (int)Math.floor(center.getZ() + ((double)(Math.cos((rotationYaw+randLook) / 180.0F * 3.1415927F)/* * Math.cos(center.rotationPitch / 180.0F * 3.1415927F)*/) * dist));

                BlockPos pos = new BlockPos(gatherX, gatherY, gatherZ);

                if (!world.hasChunkAt(pos)) return false;

                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                int tries = 0;
                if (!world.isEmptyBlock(pos)) {
                    //int offset = -5;

                    while (tries < 30) {
                        if (world.isEmptyBlock(pos) && world.isEmptyBlock(pos.above())/* || !block.isSideSolid(block.getDefaultState(), world, new BlockPos(gatherX, gatherY, gatherZ), EnumFacing.UP)*/) {
                            break;
                        }
                        gatherY += 1;//offset++;
                        pos = new BlockPos(gatherX, gatherY, gatherZ);
                        state = world.getBlockState(pos);
                        block = state.getBlock();
                        tries++;
                    }
                } else {
                    //int offset = 0;
                    while (tries < 30) {
                        if (!world.isEmptyBlock(pos) && (state.getMaterial().isSolid() || world.getBlockState(pos).getMaterial() == Material.WATER)) {
                            break;
                        }
                        gatherY -= 1;//offset++;
                        pos = new BlockPos(gatherX, gatherY, gatherZ);
                        state = world.getBlockState(pos);
                        block = state.getBlock();
                        tries++;
                    }
                }

                if (tries < 30) {
                    /*if (world.getBlockState(pos).getMaterial() == Material.WATER) {
                        gatherY--;
                    }*/
                    success = ent.getNavigation().moveTo(gatherX, gatherY, gatherZ, moveSpeedAmp);
                    //System.out.println("pp success? " + success + "- move to player: " + ent + " -> " + player);
                }
            }
        }

        return success;
    }

    public static BlockPos findBlock(MobEntity entity, int scanRange, BiPredicate<World, BlockPos> predicate) {

        int scanSize = scanRange;
        int scanSizeY = scanRange / 2;
        int adjustRangeY = 10;

        int tryX;
        int tryY = MathHelper.floor(entity.getY()) - 1;
        int tryZ;

        for (int ii = 0; ii <= 10; ii++) {
            //try close to entity first few times
            if (ii <= 3) {
                scanSize = 20;
                scanSizeY = 10 / 2;
            } else {
                scanSize = scanRange;
                scanSizeY = scanRange / 2;
            }
            tryX = MathHelper.floor(entity.getX()) + (entity.level.random.nextInt(scanSize)-scanSize/2);
            int i = tryY + entity.level.random.nextInt(scanSizeY)-(scanSizeY/2);
            tryZ = MathHelper.floor(entity.getZ()) + entity.level.random.nextInt(scanSize)-scanSize/2;
            BlockPos posTry = new BlockPos(tryX, tryY, tryZ);

            boolean foundBlock = false;
            int newY = i;

            if (!entity.level.isEmptyBlock(posTry)) {
                //scan up
                int tryMax = adjustRangeY;
                while (!entity.level.isEmptyBlock(posTry) && tryMax-- > 0) {
                    newY++;
                    posTry = new BlockPos(tryX, newY, tryZ);
                }

                //if found air and water below it
                /*if (entity.world.isAirBlock(posTry) && entity.world.getBlockState(posTry.add(0, -1, 0)).getMaterial().isLiquid()) {
                    foundWater = true;
                }*/

                if (entity.level.isEmptyBlock(posTry) && predicate.test(entity.level, posTry.offset(0, -1, 0))) {
                    foundBlock = true;
                }
            } else {
                //scan down
                int tryMax = adjustRangeY;
                while (entity.level.isEmptyBlock(posTry) && tryMax-- > 0) {
                    newY--;
                    posTry = new BlockPos(tryX, newY, tryZ);
                }
                /*if (!entity.world.isAirBlock(posTry) && entity.world.getBlockState(posTry.add(0, 1, 0)).getMaterial().isLiquid()) {
                    foundWater = true;
                }*/
                if (entity.level.isEmptyBlock(posTry.offset(0, 1, 0)) && predicate.test(entity.level, posTry)) {
                    foundBlock = true;
                }
            }

            if (foundBlock) {
                return posTry;
            }
        }

        return null;
    }

    public static boolean isWater(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.WATER;
    }

    public static boolean isDeepWater(World world, BlockPos pos) {
        boolean clearAbove = world.isEmptyBlock(pos.above(1)) && world.isEmptyBlock(pos.above(2)) && world.isEmptyBlock(pos.above(3));
        boolean deep = world.getBlockState(pos).getMaterial() == Material.WATER && world.getBlockState(pos.below()).getMaterial() == Material.WATER;
        boolean notUnderground = false;
        if (deep) {
            int height = world.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING, pos).getY() - 1;
            notUnderground = height == pos.getY();
        }

        return deep && notUnderground && clearAbove;
    }

    public static boolean isLand(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial().isSolid();
    }

    public static boolean isFire(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.FIRE;
    }

    public static double getDistance(Entity ent, double x, double y, double z)
    {
    	return ent.position().subtract(x, y, z).length();
    }

    public static Field findField(Class<?> clazz, String... fieldNames)
    {
        Exception failed = null;
        for (String fieldName : fieldNames)
        {
            try
            {
                Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f;
            }
            catch (Exception e)
            {
                failed = e;
            }
        }
        throw new UnableToFindFieldException(failed);
    }

    public static class UnableToFindFieldException extends RuntimeException
    {
        private UnableToFindFieldException(Exception e)
        {
            super(e);
        }
    }

    public static int randFlip(final Random rand, final int i) {
        return rand.nextBoolean() ? rand.nextInt(i) : -(rand.nextInt(i));
    }

    public static final String toEnglishName(String internalName) {
        return Arrays.stream(internalName.toLowerCase(Locale.ROOT).split("_"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(" "));
    }

    public static ResourceLocation resource(String location) {
        return new ResourceLocation(Constants.MODID, location);
    }

    /**
     * Removes random unique elements from provided list and returns a new list with
     * extracted elements.
     *
     * @param rand The random used for the randomization of element selection.
     * @param list The list to extract from.
     * @param amount The amount of elements to randomly extract.
     * @param <T>
     * @return A list of random elements extracted from the provided list.
     */
    public static <T> List<T> extractRandomElements(Random rand, List<T> list, int amount) {
        if (amount > list.size()) {
            throw new IllegalArgumentException("Amount of random elements can not be greater than size of provided list.");
        }

        List<T> randValues = Lists.newArrayList();

        for (int i = 0; i < amount; i++) {
            T obj = list.remove(rand.nextInt(list.size()));
            randValues.add(obj);
        }

        return randValues;
    }

    @Nullable
    public static BlockPos findGround(World world, BlockPos origin, int maximumDistance) {
        if (!isSolidGround(world, origin) && isSolidGround(world, origin.below())) {
            return origin;
        }

        // if this position is not free, scan upwards to find the ground
        if (isSolidGround(world, origin)) {
            BlockPos.Mutable mutablePos = origin.mutable();

            for (int i = 0; i < maximumDistance; i++) {
                mutablePos.move(Direction.UP);
                if (World.isOutsideBuildHeight(mutablePos) || !isSolidGround(world, mutablePos)) {
                    return mutablePos.immutable();
                }
            }
        }

        // if the position below us is not solid, scan downwards to find the ground
        if (!isSolidGround(world, origin.below())) {
            BlockPos.Mutable mutablePos = origin.mutable();

            for (int i = 0; i < maximumDistance; i++) {
                mutablePos.move(Direction.DOWN);
                if (World.isOutsideBuildHeight(mutablePos)) {
                    return null;
                }

                if (isSolidGround(world, mutablePos)) {
                    return mutablePos.move(Direction.UP).immutable();
                }
            }
        }

        return null;
    }

    private static boolean isSolidGround(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial().isSolid();
    }

    public static void drawParticleBetween(IParticleData data, Vector3d start, Vector3d end, ServerWorld world, Random random, int count, double xzScale, double yScale, double speedBase, double speedScale) {
        for (int i = 0; i < count; i++) {
            Vector3d sample = lerpVector(start, end, i / 20.0);
            double d3 = random.nextGaussian() * xzScale;
            double d1 = random.nextGaussian() * yScale;
            double d2 = random.nextGaussian() * xzScale;
            world.sendParticles(data, sample.x, sample.y, sample.z, 1 + random.nextInt(2), d3, d1, d2, speedBase + random.nextDouble() * speedScale);
        }
    }

    public static Vector3d lerpVector(Vector3d start, Vector3d end, double d) {
        return new Vector3d(MathHelper.lerp(d, start.x, end.x), MathHelper.lerp(d, start.y, end.y), MathHelper.lerp(d, start.z, end.z));
    }

    public static Direction getDirectionBetween(BlockBox from, BlockBox to) {
        BlockPos fromCenter = from.getCenterBlock();
        BlockPos toCenter = to.getCenterBlock();
        int dx = toCenter.getX() - fromCenter.getX();
        int dz = toCenter.getZ() - fromCenter.getZ();
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }
}
