package com.lovetropics.minigames.common.util;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldSettingsImport;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.OptionalInt;

public final class DynamicRegistryReadingOps {
	private static final WorldSettingsImport.IResourceAccess VOID_RESOURCE_ACCESS = new WorldSettingsImport.IResourceAccess() {
		@Override
		public Collection<ResourceLocation> getRegistryObjects(RegistryKey<? extends Registry<?>> registryKey) {
			return Collections.emptyList();
		}

		@Override
		public <E> DataResult<Pair<E, OptionalInt>> decode(DynamicOps<JsonElement> ops, RegistryKey<? extends Registry<E>> registryKey, RegistryKey<E> objectKey, Decoder<E> decoder) {
			return DataResult.error("Not looking up registry resource: we are only considering already registered entries!");
		}
	};

	public static <T> DynamicOps<T> create(MinecraftServer server, DynamicOps<T> ops) {
		DynamicRegistries.Impl dynamicRegistries = (DynamicRegistries.Impl) server.getDynamicRegistries();
		return Factory.create(ops, VOID_RESOURCE_ACCESS, dynamicRegistries);
	}

	public static <T> DynamicOps<T> create(IResourceManager resources, DynamicOps<T> ops) {
		DynamicRegistries.Impl dynamicRegistries = new DynamicRegistries.Impl();
		return Factory.create(ops, WorldSettingsImport.IResourceAccess.create(resources), dynamicRegistries);
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
				Constructor<?> constructor = WorldSettingsImport.class.getDeclaredConstructor(DynamicOps.class, WorldSettingsImport.IResourceAccess.class, DynamicRegistries.Impl.class, IdentityHashMap.class);
				constructor.setAccessible(true);

				HANDLE = MethodHandles.lookup().unreflectConstructor(constructor);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Could not reflect WorldSettingsImport constructor", e);
			}
		}

		@SuppressWarnings("unchecked")
		static <T> WorldSettingsImport<T> create(DynamicOps<T> ops, WorldSettingsImport.IResourceAccess resources, DynamicRegistries.Impl dynamicRegistries) {
			try {
				return (WorldSettingsImport<T>) HANDLE.invokeExact(ops, resources, dynamicRegistries, new IdentityHashMap<>());
			} catch (Throwable e) {
				throw new RuntimeException("Unable to create WorldSettingsImport instance", e);
			}
		}
	}
}
