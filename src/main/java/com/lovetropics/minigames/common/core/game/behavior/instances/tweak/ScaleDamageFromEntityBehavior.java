package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.predicate.entity.EntityPredicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ScaleDamageFromEntityBehavior(float factor, EntityPredicate predicate) implements IGameBehavior {
    public static final Codec<ScaleDamageFromEntityBehavior> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.floatRange(0.0F, Float.MAX_VALUE).fieldOf("factor").forGetter(ScaleDamageFromEntityBehavior::factor),
            EntityPredicate.CODEC_WITH_FALLBACK.fieldOf("source").forGetter(ScaleDamageFromEntityBehavior::predicate)
    ).apply(in, ScaleDamageFromEntityBehavior::new));

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(GamePlayerEvents.DAMAGE_AMOUNT, (player, damageSource, amount, originalAmount) -> {
            if (damageSource.getDirectEntity() != null && predicate.test(damageSource.getDirectEntity())) {
                return factor * amount;
            }
            return amount;
        });
    }
}
