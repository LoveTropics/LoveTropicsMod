package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.diguise.PlayerDisguise;
import com.lovetropics.minigames.common.core.diguise.ServerPlayerDisguises;
import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.food.FoodData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Map;

/**
 * Used to cache previous game type, dimension and position of player
 * before teleporting into minigame instance.
 * <p>
 * Can use restore() to reset them back to their previous state.
 */
public final class PlayerSnapshot {
	private final GameType gameType;
	private final ResourceKey<Level> dimension;
	private final BlockPos pos;
	private final CompoundTag playerData;

	private final PlayerTeam team;
	private final Object2IntMap<Objective> objectives = new Object2IntOpenHashMap<>();

	private final DisguiseType disguise;

	private PlayerSnapshot(ServerPlayer player) {
		this.gameType = player.gameMode.getGameModeForPlayer();
		this.dimension = player.level.dimension();
		this.pos = player.blockPosition();

		this.playerData = new CompoundTag();
		player.addAdditionalSaveData(this.playerData);
		this.playerData.remove("playerGameType");

		ServerScoreboard scoreboard = player.getLevel().getScoreboard();
		this.team = scoreboard.getPlayersTeam(player.getScoreboardName());
		for (Map.Entry<Objective, Score> entry : scoreboard.getPlayerScores(player.getScoreboardName()).entrySet()) {
			this.objectives.put(entry.getKey(), entry.getValue().getScore());
		}

		this.disguise = PlayerDisguise.getDisguiseType(player);
	}

	public static PlayerSnapshot takeAndClear(ServerPlayer player) {
		PlayerSnapshot snapshot = new PlayerSnapshot(player);
		clearPlayer(player);
		return snapshot;
	}

	public static void clearPlayer(ServerPlayer player) {
		player.getInventory().clearContent();
		player.setHealth(player.getMaxHealth());

		player.removeAllEffects();
		player.setGlowingTag(false);
		player.setArrowCount(0);
		player.fallDistance = 0.0F;

		CompoundTag foodTag = new CompoundTag();
		new FoodData().addAdditionalSaveData(foodTag);
		player.getFoodData().readAdditionalSaveData(foodTag);

		ServerPlayerDisguises.clear(player);

		ServerScoreboard scoreboard = player.getLevel().getScoreboard();
		scoreboard.removePlayerFromTeam(player.getScoreboardName());

		Map<Objective, Score> objectives = scoreboard.getPlayerScores(player.getScoreboardName());
		for (Objective objective : new ArrayList<>(objectives.keySet())) {
			scoreboard.resetPlayerScore(player.getScoreboardName(), objective);
		}
	}

	/**
	 * Resets the player back to their previous state when this cache
	 * was created.
	 *
	 * @param player The player being reset.
	 */
	public void restore(ServerPlayer player) {
		clearPlayer(player);

		player.readAdditionalSaveData(this.playerData);
		player.setGameMode(this.gameType);

		DimensionUtils.teleportPlayerNoPortal(player, this.dimension, this.pos);

		PlayerDisguise.get(player).ifPresent(disguise -> disguise.setDisguise(this.disguise));

		ServerScoreboard scoreboard = player.getLevel().getScoreboard();

		if (this.team != null) {
			scoreboard.addPlayerToTeam(player.getScoreboardName(), this.team);
		}

		for (Object2IntMap.Entry<Objective> entry : this.objectives.object2IntEntrySet()) {
			Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), entry.getKey());
			score.setScore(entry.getIntValue());
		}

		player.containerMenu.broadcastFullState();
	}
}
