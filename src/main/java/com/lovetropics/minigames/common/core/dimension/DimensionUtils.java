package com.lovetropics.minigames.common.core.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;

public class DimensionUtils {
    public static void teleportPlayerNoPortal(ServerPlayer player, ResourceKey<Level> destination, BlockPos pos) {
		if (!ForgeHooks.onTravelToDimension(player, destination)) return;

		ServerLevel world = player.server.getLevel(destination);
		player.teleportTo(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, player.getYRot(), player.getXRot());

		ForgeEventFactory.firePlayerChangedDimensionEvent(player, destination, destination);
	}

	public static Holder<DimensionType> overworld(MinecraftServer server) {
    	return server.overworld().dimensionTypeRegistration();
	}
}
