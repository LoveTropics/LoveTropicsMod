package com.lovetropics.minigames.common.content.biodiversity_blitz;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Simple marker for explosions that affect mobs but not players
 */
public class FriendlyExplosion extends Explosion {
    
    public FriendlyExplosion(World world, @Nullable Entity exploder, @Nullable DamageSource source, @Nullable ExplosionContext context, double x, double y, double z, float size, boolean causesFire, Mode mode) {
        super(world, exploder, source, context, x, y, z, size, causesFire, mode);
    }
}
