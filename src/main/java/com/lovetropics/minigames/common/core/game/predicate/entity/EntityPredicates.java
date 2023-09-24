package com.lovetropics.minigames.common.core.game.predicate.entity;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.mojang.serialization.Codec;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class EntityPredicates {
    public static final ResourceKey<Registry<Codec<? extends EntityPredicate>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(Constants.MODID, "entity_predicates"));
    public static final DeferredRegister<Codec<? extends EntityPredicate>> REGISTER = DeferredRegister.create(REGISTRY_KEY, Constants.MODID);

    public static final Supplier<IForgeRegistry<Codec<? extends EntityPredicate>>> REGISTRY = REGISTER.makeRegistry(() -> new RegistryBuilder<Codec<? extends EntityPredicate>>()
            .disableSync()
            .disableSaving());

    private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

    public static final RegistryEntry<Codec<EntityTypeEntityPredicate>> ENTITY_TYPE = register("entity_type", EntityTypeEntityPredicate.CODEC);

    public static <T extends EntityPredicate> RegistryEntry<Codec<T>> register(final String name, final Codec<T> codec) {
        return REGISTRATE.object(name).entityPredicate(codec).register();
    }

    public static void init(IEventBus modBus) {
        REGISTER.register(modBus);
    }
}
