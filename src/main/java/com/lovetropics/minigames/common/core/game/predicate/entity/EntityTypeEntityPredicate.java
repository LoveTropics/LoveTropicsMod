package com.lovetropics.minigames.common.core.game.predicate.entity;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record EntityTypeEntityPredicate(EntityType<?> type) implements EntityPredicate {
    public static final Codec<EntityTypeEntityPredicate> CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec()
            .fieldOf("entity_type").xmap(EntityTypeEntityPredicate::new, EntityTypeEntityPredicate::type)
            .codec();

    @Override
    public boolean test(Entity entity) {
        return entity.getType() == type;
    }

    @Override
    public Codec<? extends EntityPredicate> codec() {
        return CODEC;
    }
}
