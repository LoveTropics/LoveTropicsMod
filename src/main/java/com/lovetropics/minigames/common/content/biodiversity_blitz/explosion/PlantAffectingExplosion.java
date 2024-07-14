package com.lovetropics.minigames.common.content.biodiversity_blitz.explosion;

import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state.PlantHealth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class PlantAffectingExplosion extends FilteredExplosion {
    private final double x;
    private final double y;
    private final double z;
    private final Plot plot;

    public PlantAffectingExplosion(Level world, @Nullable Entity exploder, @Nullable DamageSource source, @Nullable ExplosionDamageCalculator context, double x, double y, double z, float size, boolean causesFire, BlockInteraction mode, ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles, Holder<SoundEvent> sound, Predicate<Entity> remove, Plot plot) {
        super(world, exploder, source, context, x, y, z, size, causesFire, mode, smallExplosionParticles, largeExplosionParticles, sound, remove);
        this.x = x;
        this.y = y;
        this.z = z;
        this.plot = plot;
    }

    public void affectPlants(List<BlockPos> affectedBlocks) {
        Vec3 center = new Vec3(this.x, this.y, this.z);

        Random random = new Random();
        for (BlockPos pos : affectedBlocks) {
            Vec3 vec = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            double distance = vec.distanceToSqr(center);
            // TODO: damage should scale based on radius
            double damage = 80.0 / (distance + 1);
            // Randomize damage a bit to leave certain plants standing
            damage *= (1 + ((random.nextDouble() - random.nextDouble()) * 0.3));

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
