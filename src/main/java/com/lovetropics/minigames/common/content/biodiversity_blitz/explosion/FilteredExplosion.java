package com.lovetropics.minigames.common.content.biodiversity_blitz.explosion;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.Predicate;

import net.minecraft.world.level.Explosion.BlockInteraction;

/**
 * Simple marker for explosions that affect mobs but not players
 */
public class FilteredExplosion extends Explosion {
    public final Predicate<Entity> remove;

    public FilteredExplosion(Level world, @Nullable Entity exploder, @Nullable DamageSource source, @Nullable ExplosionDamageCalculator context, double x, double y, double z, float size, boolean causesFire, BlockInteraction mode, Predicate<Entity> remove) {
        super(world, exploder, source, context, x, y, z, size, causesFire, mode);
        this.remove = remove;
    }
}
