package com.lovetropics.minigames.common.core.dimension;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.hooks.BasicEventHooks;

import java.util.function.Supplier;

public class DimensionUtils {
    public static void teleportPlayerNoPortal(ServerPlayer player, ResourceKey<Level> destination, BlockPos pos) {
		if (!ForgeHooks.onTravelToDimension(player, destination)) return;

		ServerLevel world = player.server.getLevel(destination);
		player.teleportTo(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, player.yRot, player.xRot);

		BasicEventHooks.firePlayerChangedDimensionEvent(player, destination, destination);
	}

	public static Supplier<DimensionType> overworld(MinecraftServer server) {
    	return () -> server.overworld().dimensionType();
	}
}
