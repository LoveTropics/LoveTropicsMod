package com.lovetropics.minigames.common.core.game.behavior.action;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.predicate.entity.EntityPredicate;
import com.lovetropics.minigames.common.core.game.predicate.entity.EntityTypeEntityPredicate;
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
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ActionTargetTypes {
    public static final ResourceKey<Registry<Codec<? extends ActionTarget<?>>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(Constants.MODID, "action_target_types"));
    public static final DeferredRegister<Codec<? extends ActionTarget<?>>> REGISTER = DeferredRegister.create(REGISTRY_KEY, Constants.MODID);

    public static final Supplier<IForgeRegistry<Codec<? extends ActionTarget<?>>>> REGISTRY = REGISTER.makeRegistry(() -> new RegistryBuilder<Codec<? extends ActionTarget<?>>>()
            .disableSync()
            .disableSaving());

    private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

    public static final RegistryObject<Codec<PlayerActionTarget>> PLAYER = register("player", PlayerActionTarget.CODEC);
    public static final RegistryObject<Codec<PlotActionTarget>> PLOT = register("plot", PlotActionTarget.CODEC);
    public static final RegistryObject<Codec<NoneActionTarget>> NONE = register("none", NoneActionTarget.CODEC);

    public static <T extends ActionTarget<?>> RegistryObject<Codec<T>> register(final String name, final Codec<T> codec) {
        return REGISTER.register(name, () -> codec);
    }

    public static void init(IEventBus modBus) {
        REGISTER.register(modBus);
    }
}
