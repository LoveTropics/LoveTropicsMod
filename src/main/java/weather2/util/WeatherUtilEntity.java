package weather2.util;

import CoroUtil.util.CoroUtilEntOrParticle;
import extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;

public class WeatherUtilEntity {
	
    public static float getWeight(Object entity1)
    {
    	World world = CoroUtilEntOrParticle.getWorld(entity1);

        if (world == null) {
            return 1F;
        }

		if (isParticleRotServerSafe(world, entity1))
		{
			float var = WeatherUtilParticle.getParticleWeight((EntityRotFX)entity1);

			if (var != -1)
			{
				return var;
			}
		}

		if (entity1 instanceof SquidEntity)
		{
			return 400F;
		}

        if (entity1 instanceof LivingEntity)
        {
        	LivingEntity livingEnt = (LivingEntity) entity1;
        	int airTime = livingEnt.getPersistentData().getInt("timeInAir");
        	if (livingEnt.isOnGround() || livingEnt.isInWater())
            {
                airTime = 0;
            }
            else {
            	airTime++;
            }
        	
        	livingEnt.getPersistentData().putInt("timeInAir", airTime);

			if (entity1 instanceof PlayerEntity) {
				if (((PlayerEntity) entity1).abilities.isCreativeMode) return 99999999F;
				return 5.0F + airTime / 400.0F;
			} else {
				return 500.0F + (livingEnt.isOnGround() ? 2.0F : 0.0F) + (airTime / 400.0F);
			}
        }

        if (entity1 instanceof BoatEntity || entity1 instanceof ItemEntity || entity1 instanceof FishingBobberEntity)
        {
            return 4000F;
        }

        if (entity1 instanceof AbstractMinecartEntity)
        {
            return 80F;
        }

        return 1F;
    }
    
    public static boolean isParticleRotServerSafe(World world, Object obj) {
    	if (EffectiveSide.get().equals(LogicalSide.SERVER)) {
    		return false;
    	}
    	if (!world.isRemote) return false;
    	return isParticleRotClientCheck(obj);
    }
    
    public static boolean isParticleRotClientCheck(Object obj) {
    	return obj instanceof EntityRotFX;
    }
    
    public static double getDistanceSqEntToPos(Entity ent, BlockPos pos) {
    	return ent.getPositionVec().squareDistanceTo(Vector3d.copyCentered(pos));
    }
}
