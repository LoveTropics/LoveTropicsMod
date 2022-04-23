package com.lovetropics.minigames.common.core.dimension;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.hooks.BasicEventHooks;

import java.util.function.Supplier;

public class DimensionUtils {
    public static void teleportPlayerNoPortal(ServerPlayerEntity player, RegistryKey<World> destination, BlockPos pos) {
		if (!ForgeHooks.onTravelToDimension(player, destination)) return;

		ServerWorld world = player.server.getLevel(destination);
		player.teleportTo(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, player.yRot, player.xRot);

		BasicEventHooks.firePlayerChangedDimensionEvent(player, destination, destination);
	}

	public static Supplier<DimensionType> overworld(MinecraftServer server) {
    	return () -> server.overworld().dimensionType();
	}
}
