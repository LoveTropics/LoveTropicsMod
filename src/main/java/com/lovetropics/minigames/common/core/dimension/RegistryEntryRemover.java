package com.lovetropics.minigames.common.core.dimension;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.MappedRegistry;

public interface RegistryEntryRemover<T> {
    @SuppressWarnings("unchecked")
    static <T> boolean remove(MappedRegistry<T> registry, ResourceLocation key) {
        return ((RegistryEntryRemover<T>) registry).remove(key);
    }

    @SuppressWarnings("unchecked")
    static <T> boolean remove(MappedRegistry<T> registry, T value) {
        return ((RegistryEntryRemover<T>) registry).remove(value);
    }

    boolean remove(T value);

    boolean remove(ResourceLocation key);
}
