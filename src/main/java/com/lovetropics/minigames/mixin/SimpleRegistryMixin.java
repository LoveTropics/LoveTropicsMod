package com.lovetropics.minigames.mixin;

import com.google.common.collect.BiMap;
import com.lovetropics.minigames.common.dimension.RegistryEntryRemover;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(SimpleRegistry.class)
public class SimpleRegistryMixin<T> implements RegistryEntryRemover<T> {
    @Shadow @Final private BiMap<ResourceLocation, T> registryObjects;
    @Shadow @Final private BiMap<RegistryKey<T>, T> keyToObjectMap;
    @Shadow @Final private Object2IntMap<T> entryIndexMap;
    @Shadow @Final private ObjectList<T> entryList;
    @Shadow @Final private Map<T, Lifecycle> objectToLifecycleMap;
    @Shadow protected Object[] values;

    @Override
    public boolean remove(T entry) {
        int rawId = this.entryIndexMap.removeInt(entry);
        if (rawId == -1) {
            return false;
        }

        this.keyToObjectMap.inverse().remove(entry);
        this.registryObjects.inverse().remove(entry);
        this.objectToLifecycleMap.remove(entry);

        this.entryList.set(rawId, null);

        this.values = null;

        return true;
    }

    @Override
    public boolean remove(ResourceLocation key) {
        T entry = this.registryObjects.get(key);
        return entry != null && this.remove(entry);
    }
}
