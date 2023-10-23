package com.lovetropics.minigames.common.util.registry;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// Replicates behavior of dynamic registries - won't be needed once we switch to use those
public class RegistryLoadingOps {
	private static <T> RegistryOps.RegistryInfo<T> createLoadingRegistryInfo(Registry<T> registry) {
		return new RegistryOps.RegistryInfo<>(registry.asLookup(), new HolderGetter<>() {
			@Override
			public Optional<Holder.Reference<T>> get(ResourceKey<T> key) {
				return registry.getHolder(key);
			}

			@Override
			public Optional<HolderSet.Named<T>> get(TagKey<T> tagKey) {
				return Optional.of(registry.getOrCreateTag(tagKey));
			}
		}, registry.registryLifecycle());
	}

	public static <T> DynamicOps<T> create(DynamicOps<T> ops, RegistryAccess registryAccess) {
		Map<ResourceKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> registryInfo = registryAccess.registries()
				.map(registry -> Pair.of(
						registry.key(),
						createLoadingRegistryInfo(registry.value())
				))
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
		return RegistryOps.create(ops, new RegistryOps.RegistryInfoLookup() {
			@Override
			@SuppressWarnings("unchecked")
			public <V> Optional<RegistryOps.RegistryInfo<V>> lookup(ResourceKey<? extends Registry<? extends V>> key) {
				return Optional.ofNullable((RegistryOps.RegistryInfo<V>) registryInfo.get(key));
			}
		});
	}
}
