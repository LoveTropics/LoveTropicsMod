package com.lovetropics.minigames.common.util;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryReadOps;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.OptionalInt;

public final class DynamicRegistryReadingOps {
	private static final RegistryReadOps.ResourceAccess VOID_RESOURCE_ACCESS = new RegistryReadOps.ResourceAccess() {
		@Override
		public Collection<ResourceLocation> listResources(ResourceKey<? extends Registry<?>> registryKey) {
			return Collections.emptyList();
		}

		@Override
		public <E> DataResult<Pair<E, OptionalInt>> parseElement(DynamicOps<JsonElement> ops, ResourceKey<? extends Registry<E>> registryKey, ResourceKey<E> objectKey, Decoder<E> decoder) {
			return DataResult.error("Not looking up registry resource: we are only considering already registered entries!");
		}
	};

	public static <T> DynamicOps<T> create(MinecraftServer server, DynamicOps<T> ops) {
		RegistryAccess.RegistryHolder dynamicRegistries = (RegistryAccess.RegistryHolder) server.registryAccess();
		return Factory.create(ops, VOID_RESOURCE_ACCESS, dynamicRegistries);
	}

	public static <T> DynamicOps<T> create(ResourceManager resources, DynamicOps<T> ops) {
		RegistryAccess.RegistryHolder dynamicRegistries = new RegistryAccess.RegistryHolder();
		return Factory.create(ops, RegistryReadOps.ResourceAccess.forResourceManager(resources), dynamicRegistries);
	}

	/**
	 * We need to reflectively construct the WorldSettingsImport type because by default the factory method initializes
	 * the DynamicRegistries from the datapack, which we do not need to do in our simple case! We just want to be able
	 * to reference dynamic registry content *after* world initialization, which is what this allows us to do.
	 */
	static final class Factory {
		private static final MethodHandle HANDLE;

		static {
			try {
				Constructor<?> constructor = RegistryReadOps.class.getDeclaredConstructor(DynamicOps.class, RegistryReadOps.ResourceAccess.class, RegistryAccess.RegistryHolder.class, IdentityHashMap.class);
				constructor.setAccessible(true);

				HANDLE = MethodHandles.lookup().unreflectConstructor(constructor);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Could not reflect WorldSettingsImport constructor", e);
			}
		}

		@SuppressWarnings("unchecked")
		static <T> RegistryReadOps<T> create(DynamicOps<T> ops, RegistryReadOps.ResourceAccess resources, RegistryAccess.RegistryHolder dynamicRegistries) {
			try {
				return (RegistryReadOps<T>) HANDLE.invokeExact(ops, resources, dynamicRegistries, new IdentityHashMap<>());
			} catch (Throwable e) {
				throw new RuntimeException("Unable to create WorldSettingsImport instance", e);
			}
		}
	}
}
