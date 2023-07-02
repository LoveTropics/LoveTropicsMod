package com.lovetropics.minigames.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TagManager.class)
public interface TagManagerAccessor {
	@Accessor("registryAccess")
	RegistryAccess ltminigames$getRegistryAccess();
}
