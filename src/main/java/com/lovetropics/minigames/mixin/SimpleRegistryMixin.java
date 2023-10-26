package com.lovetropics.minigames.mixin;

import com.lovetropics.minigames.common.core.dimension.RegistryEntryRemover;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(MappedRegistry.class)
public class SimpleRegistryMixin<T> implements RegistryEntryRemover<T> {
    @Shadow @Final private ObjectList<Holder.Reference<T>> byId;
    @Shadow @Final private Object2IntMap<T> toId;
    @Shadow @Final private Map<ResourceLocation, Holder.Reference<T>> byLocation;
    @Shadow @Final private Map<ResourceKey<T>, Holder.Reference<T>> byKey;
    @Shadow @Final private Map<T, Holder.Reference<T>> byValue;
    @Shadow @Final private Map<T, Lifecycle> lifecycles;
    @Shadow protected Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;
    @Shadow private List<Holder.Reference<T>> holdersInOrder;

    @Override
    public boolean remove(T entry) {
        int rawId = this.toId.removeInt(entry);
        if (rawId == -1) {
            return false;
        }

        final Holder.Reference<T> reference = this.byId.set(rawId, null);
        final ResourceKey<T> key = reference.key();

        this.byLocation.remove(key.location());
        this.byKey.remove(key);
        this.byValue.remove(entry);
        this.lifecycles.remove(entry);
        if (this.unregisteredIntrusiveHolders != null) {
            this.unregisteredIntrusiveHolders.remove(entry);
        }
        if (this.holdersInOrder != null) {
            this.holdersInOrder = null;
        }

        return true;
    }

    @Override
    public boolean remove(ResourceLocation key) {
        Holder.Reference<T> entry = this.byLocation.get(key);
        return entry != null && this.remove(entry.value());
    }
}
