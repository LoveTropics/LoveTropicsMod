package com.lovetropics.minigames.common.minigames.map;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class MinigameMapProviderTypes {
	public static final DeferredRegister<MinigameMapProviderType> REGISTER = DeferredRegister.create(MinigameMapProviderType.class, "ltminigames");
	public static final Supplier<IForgeRegistry<MinigameMapProviderType>> REGISTRY;

	public static final RegistryObject<MinigameMapProviderType> LOAD_MAP;
	public static final RegistryObject<MinigameMapProviderType> RANDOM;
	public static final RegistryObject<MinigameMapProviderType> INLINE;

	public static <T extends IMinigameMapProvider> RegistryObject<MinigameMapProviderType> register(final String name, final MinigameMapProviderType.Factory instanceFactory) {
		return REGISTER.register(name, () -> new MinigameMapProviderType(instanceFactory));
	}

	static {
		REGISTRY = REGISTER.makeRegistry("minigame_map_providers", RegistryBuilder::new);
		LOAD_MAP = register("load_map", LoadMapProvider::parse);
		RANDOM = register("random", RandomMapProvider::parse);
		INLINE = register("inline", InlineMapProvider::parse);
	}
}
