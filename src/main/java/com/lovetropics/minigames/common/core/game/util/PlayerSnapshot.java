package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.diguise.PlayerDisguise;
import com.lovetropics.minigames.common.core.diguise.ServerPlayerDisguises;
import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.FoodStats;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

/**
 * Used to cache previous game type, dimension and position of player
 * before teleporting into minigame instance.
 * <p>
 * Can use restore() to reset them back to their previous state.
 */
public final class PlayerSnapshot {
	private final GameType gameType;
	private final RegistryKey<World> dimension;
	private final BlockPos pos;
	private final CompoundNBT playerData;

	private final ScorePlayerTeam team;

	private final DisguiseType disguise;

	private PlayerSnapshot(ServerPlayerEntity player) {
		this.gameType = player.interactionManager.getGameType();
		this.dimension = player.world.getDimensionKey();
		this.pos = player.getPosition();

		this.playerData = new CompoundNBT();
		player.writeAdditional(this.playerData);

		this.team = player.getWorldScoreboard().getPlayersTeam(player.getScoreboardName());

		this.disguise = PlayerDisguise.getDisguiseType(player);
	}

	public static PlayerSnapshot takeAndClear(ServerPlayerEntity player) {
		PlayerSnapshot snapshot = new PlayerSnapshot(player);
		clearPlayer(player);
		return snapshot;
	}

	public static void clearPlayer(ServerPlayerEntity player) {
		player.inventory.clear();
		player.setHealth(player.getMaxHealth());

		player.clearActivePotions();
		player.setGlowing(false);

		CompoundNBT foodTag = new CompoundNBT();
		new FoodStats().write(foodTag);
		player.getFoodStats().read(foodTag);

		player.getWorldScoreboard().removePlayerFromTeams(player.getScoreboardName());

		ServerPlayerDisguises.clear(player);
	}

	/**
	 * Resets the player back to their previous state when this cache
	 * was created.
	 *
	 * @param player The player being reset.
	 */
	public void restore(ServerPlayerEntity player) {
		clearPlayer(player);

		player.readAdditional(this.playerData);
		player.setGameType(this.gameType);

		DimensionUtils.teleportPlayerNoPortal(player, this.dimension, this.pos);

		if (this.team != null) {
			player.getWorldScoreboard().addPlayerToTeam(player.getScoreboardName(), this.team);
		}

		PlayerDisguise.get(player).ifPresent(disguise -> disguise.setDisguise(this.disguise));
	}
}
