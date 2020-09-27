package com.lovetropics.minigames.common.minigames.map;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class MinigameMapProviderTypes {
	public static final DeferredRegister<MinigameMapProviderType<?>> REGISTER = DeferredRegister.create(MinigameMapProviderType.wildcardType(), "ltminigames");
	public static final Supplier<IForgeRegistry<MinigameMapProviderType<?>>> REGISTRY;

	public static final RegistryObject<MinigameMapProviderType<LoadMapProvider>> LOAD_MAP;

	public static <T extends IMinigameMapProvider> RegistryObject<MinigameMapProviderType<T>> register(final String name, final MinigameMapProviderType.Factory<T> instanceFactory) {
		return REGISTER.register(name, () -> new MinigameMapProviderType<T>(instanceFactory));
	}

	static {
		REGISTRY = REGISTER.makeRegistry("minigame_map_providers", RegistryBuilder::new);
		LOAD_MAP = register("load_map", LoadMapProvider::parse);
	}
}
