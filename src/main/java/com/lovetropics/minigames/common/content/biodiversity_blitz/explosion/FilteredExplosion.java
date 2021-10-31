package com.lovetropics.minigames.common.content.biodiversity_blitz.explosion;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * Simple marker for explosions that affect mobs but not players
 */
public class FilteredExplosion extends Explosion {
    public final Predicate<Entity> remove;

    public FilteredExplosion(World world, @Nullable Entity exploder, @Nullable DamageSource source, @Nullable ExplosionContext context, double x, double y, double z, float size, boolean causesFire, Mode mode, Predicate<Entity> remove) {
        super(world, exploder, source, context, x, y, z, size, causesFire, mode);
        this.remove = remove;
    }
}
