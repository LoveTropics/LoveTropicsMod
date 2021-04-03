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

	@Nullable
	default IActiveGame getActiveGameFor(PlayerEntity player) {
		IGameInstance game = getGameFor(player);
		return game != null ? game.asActive() : null;
	}

	@Nullable
	default IActiveGame getActiveGameFor(Entity entity) {
		IGameInstance game = getGameFor(entity);
		return game != null ? game.asActive() : null;
	}

	@Nullable
	default IActiveGame getActiveGameAt(World world, BlockPos pos) {
		IGameInstance game = getGameAt(world, pos);
		return game != null ? game.asActive() : null;
	}

	@Nullable
	default IActiveGame getActiveGameFor(CommandSource source) {
		IGameInstance game = getGameFor(source);
		return game != null ? game.asActive() : null;
	}

	@Nullable
	default IPollingGame getPollingGameFor(PlayerEntity player) {
		IGameInstance game = getGameFor(player);
		return game != null ? game.asPolling() : null;
	}

	@Nullable
	default IPollingGame getPollingGameFor(Entity entity) {
		IGameInstance game = getGameFor(entity);
		return game != null ? game.asPolling() : null;
	}

	@Nullable
	default IPollingGame getPollingGameAt(World world, BlockPos pos) {
		IGameInstance game = getGameAt(world, pos);
		return game != null ? game.asPolling() : null;
	}

	@Nullable
	default IPollingGame getPollingGameFor(CommandSource source) {
		IGameInstance game = getGameFor(source);
		return game != null ? game.asPolling() : null;
	}
}
