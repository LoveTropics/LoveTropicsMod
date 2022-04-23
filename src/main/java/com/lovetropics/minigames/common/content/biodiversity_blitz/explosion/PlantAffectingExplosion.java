package com.lovetropics.minigames.common.content.biodiversity_blitz.explosion;

import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state.PlantHealth;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class PlantAffectingExplosion extends FilteredExplosion {
    private final double x;
    private final double y;
    private final double z;
    private final Plot plot;

    public PlantAffectingExplosion(World world, @Nullable Entity exploder, @Nullable DamageSource source, @Nullable ExplosionContext context, double x, double y, double z, float size, boolean causesFire, Mode mode, Predicate<Entity> remove, Plot plot) {
        super(world, exploder, source, context, x, y, z, size, causesFire, mode, remove);
        this.x = x;
        this.y = y;
        this.z = z;
        this.plot = plot;
    }

    public void affectPlants(List<BlockPos> affectedBlocks) {
        Vector3d center = new Vector3d(this.x, this.y, this.z);

        for (BlockPos pos : affectedBlocks) {
            Vector3d vec = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            double distance = vec.distanceToSqr(center);
            double damage = 160.0 / (distance + 1);

            Plant plant = this.plot.plants.getPlantAt(pos);
            if (plant != null) {
                PlantHealth health = plant.state(PlantHealth.KEY);

                if (health != null) {
                    health.decrement((int) damage);
                }
            }
        }
    }
}
