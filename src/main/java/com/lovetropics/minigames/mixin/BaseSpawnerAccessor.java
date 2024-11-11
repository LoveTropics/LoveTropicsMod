package com.lovetropics.minigames.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BaseSpawner.class)
public interface BaseSpawnerAccessor {
	@Invoker("delay")
	void invokeDelay(Level level, BlockPos pos);
}
