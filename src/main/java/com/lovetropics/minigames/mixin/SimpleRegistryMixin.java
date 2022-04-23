package com.lovetropics.minigames.mixin;

import com.google.common.collect.BiMap;
import com.lovetropics.minigames.common.core.dimension.RegistryEntryRemover;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(MappedRegistry.class)
public class SimpleRegistryMixin<T> implements RegistryEntryRemover<T> {
    @Shadow @Final private BiMap<ResourceLocation, T> storage;
    @Shadow @Final private BiMap<ResourceKey<T>, T> keyStorage;
    @Shadow @Final private Object2IntMap<T> toId;
    @Shadow @Final private ObjectList<T> byId;
    @Shadow @Final private Map<T, Lifecycle> lifecycles;
    @Shadow protected Object[] randomCache;

    @Override
    public boolean remove(T entry) {
        int rawId = this.toId.removeInt(entry);
        if (rawId == -1) {
            return false;
        }

        this.keyStorage.inverse().remove(entry);
        this.storage.inverse().remove(entry);
        this.lifecycles.remove(entry);

        this.byId.set(rawId, null);

        this.randomCache = null;

        return true;
    }

    @Override
    public boolean remove(ResourceLocation key) {
        T entry = this.storage.get(key);
        return entry != null && this.remove(entry);
    }
}
