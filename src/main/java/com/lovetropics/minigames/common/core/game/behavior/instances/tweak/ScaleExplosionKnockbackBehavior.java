package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;

public record ScaleExplosionKnockbackBehavior(float factor, EntityPredicate explodedPredicate, EntityPredicate exploderPredicate) implements IGameBehavior {
    public static final MapCodec<ScaleExplosionKnockbackBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("factor").forGetter(ScaleExplosionKnockbackBehavior::factor),
            EntityPredicate.CODEC.fieldOf("exploded").forGetter(ScaleExplosionKnockbackBehavior::explodedPredicate),
            EntityPredicate.CODEC.fieldOf("exploder").forGetter(ScaleExplosionKnockbackBehavior::exploderPredicate)
    ).apply(in, ScaleExplosionKnockbackBehavior::new));

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(GameLivingEntityEvents.MODIFY_EXPLOSION_KNOCKBACK, (entity, explosion, knockback, originalKnockback) -> {
            if (matches(game, entity, explosion)) {
                return knockback.scale(factor);
            }
            return knockback;
		});
    }

    private boolean matches(IGamePhase game, Entity entity, Explosion explosion) {
        if (!explodedPredicate.matches(game.level(), explosion.center(), entity)) {
            return false;
        }
		return explosion.getDirectSourceEntity() != null && exploderPredicate.matches(game.level(), explosion.center(), explosion.getDirectSourceEntity());
	}
}
