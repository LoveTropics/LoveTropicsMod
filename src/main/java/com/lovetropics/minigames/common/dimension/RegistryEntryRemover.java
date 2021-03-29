package com.lovetropics.minigames.common.dimension;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.SimpleRegistry;

public interface RegistryEntryRemover<T> {
    @SuppressWarnings("unchecked")
    static <T> boolean remove(SimpleRegistry<T> registry, ResourceLocation key) {
        return ((RegistryEntryRemover<T>) registry).remove(key);
    }

    @SuppressWarnings("unchecked")
    static <T> boolean remove(SimpleRegistry<T> registry, T value) {
        return ((RegistryEntryRemover<T>) registry).remove(value);
    }

    boolean remove(T value);

    boolean remove(ResourceLocation key);
}
