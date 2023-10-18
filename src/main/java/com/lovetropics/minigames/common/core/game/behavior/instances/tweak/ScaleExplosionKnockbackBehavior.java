package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.lovetropics.minigames.common.core.game.predicate.entity.EntityPredicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ScaleExplosionKnockbackBehavior(float factor, EntityPredicate exploderPredicate) implements IGameBehavior {
    public static final MapCodec<ScaleExplosionKnockbackBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("factor").forGetter(ScaleExplosionKnockbackBehavior::factor),
            EntityPredicate.CODEC_WITH_FALLBACK.fieldOf("exploder").forGetter(ScaleExplosionKnockbackBehavior::exploderPredicate)
    ).apply(in, ScaleExplosionKnockbackBehavior::new));

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(GameLivingEntityEvents.MODIFY_EXPLOSION_KNOCKBACK, (entity, explosion, knockback, originalKnockback) -> {
            if (explosion.getExploder() != null && exploderPredicate.test(explosion.getExploder())) {
                return factor * knockback;
            }
            return knockback;
        });
    }
}