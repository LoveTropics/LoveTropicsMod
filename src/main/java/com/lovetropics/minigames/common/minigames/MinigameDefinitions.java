package com.lovetropics.minigames.common.minigames;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class MinigameDefinitions
{
	public static final DeferredRegister<IMinigameDefinition> MINIGAMES_REGISTER;
	public static final Supplier<IForgeRegistry<IMinigameDefinition>> MINIGAMES_REGISTRY;

	static {
		MINIGAMES_REGISTER = DeferredRegister.create(IMinigameDefinition.class, "ltminigames");
		MINIGAMES_REGISTRY = MINIGAMES_REGISTER.makeRegistry("minigames", RegistryBuilder::new);


	}
}
