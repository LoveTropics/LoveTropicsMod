package com.lovetropics.minigames.common.core.game;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface IGameLookup {
	@Nullable
	IGameInstance getGameFor(PlayerEntity player);

	@Nullable
	IGameInstance getGameFor(Entity entity);

	@Nullable
	IGameInstance getGameAt(World world, BlockPos pos);

	@Nullable
	default IGameInstance getGameFor(CommandSource source) {
		Entity entity = source.getEntity();
		if (entity != null) {
			return getGameFor(entity);
		}
		return null;
	}
}
