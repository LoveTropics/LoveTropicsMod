package com.lovetropics.minigames.common.dimension;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.hooks.BasicEventHooks;

public class DimensionUtils {
    public static void teleportPlayerNoPortal(ServerPlayerEntity player, RegistryKey<World> destination, BlockPos pos) {
		if (!ForgeHooks.onTravelToDimension(player, destination)) return;

		ServerWorld world = player.server.getWorld(destination);
		player.teleport(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, player.rotationYaw, player.rotationPitch);

		BasicEventHooks.firePlayerChangedDimensionEvent(player, destination, destination);
	}
}
