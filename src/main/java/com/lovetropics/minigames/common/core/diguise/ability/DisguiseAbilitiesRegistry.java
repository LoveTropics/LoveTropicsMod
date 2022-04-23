package com.lovetropics.minigames.common.core.diguise.ability;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;

public final class DisguiseAbilitiesRegistry {
	private static final Multimap<RegistryObject<EntityType<?>>, DisguiseAbilities> REGISTRY = HashMultimap.create();

	private static final RegistryObject<EntityType<?>> GREEN_BASILISK_LIZARD = entityObject(new ResourceLocation("tropicraft", "green_basilisk_lizard"));
	private static final RegistryObject<EntityType<?>> BROWN_BASILISK_LIZARD = entityObject(new ResourceLocation("tropicraft", "brown_basilisk_lizard"));

	static {
	}

	public static void register(EntityType<?> entityType, DisguiseAbilities abilities) {
		register(entityObject(entityType), abilities);
	}

	public static void register(RegistryObject<EntityType<?>> entityType, DisguiseAbilities abilities) {
		REGISTRY.put(entityType, abilities);
	}

	public static DisguiseAbilities create(EntityType<?> entityType) {
		Collection<DisguiseAbilities> abilities = REGISTRY.get(entityObject(entityType));
		return new DisguiseAbilities.Composite(abilities.toArray(new DisguiseAbilities[0]));
	}

	private static RegistryObject<EntityType<?>> entityObject(ResourceLocation entityType) {
		return RegistryObject.of(entityType, ForgeRegistries.ENTITIES);
	}

	private static RegistryObject<EntityType<?>> entityObject(EntityType<?> entityType) {
		return RegistryObject.of(entityType.getRegistryName(), ForgeRegistries.ENTITIES);
	}
}
