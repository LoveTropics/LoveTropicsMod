package com.lovetropics.minigames.common.core.game.predicate.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

public interface EntityPredicate {
    Codec<EntityPredicate> DIRECT_CODEC = ExtraCodecs.lazyInitializedCodec(() -> EntityPredicates.REGISTRY.get().getCodec())
            .dispatch(EntityPredicate::codec, Function.identity());
    Codec<EntityPredicate> CODEC_WITH_FALLBACK = ExtraCodecs.xor(DIRECT_CODEC, BuiltInRegistries.ENTITY_TYPE.byNameCodec())
            .xmap(either -> either.map(Function.identity(), EntityTypeEntityPredicate::new), Either::left);

    boolean test(Entity entity);

    Codec<? extends EntityPredicate> codec();
}
