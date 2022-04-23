package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.diguise.PlayerDisguise;
import com.lovetropics.minigames.common.core.diguise.ServerPlayerDisguises;
import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.util.FoodStats;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

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
	private final RegistryKey<World> dimension;
	private final BlockPos pos;
	private final CompoundNBT playerData;

	private final ScorePlayerTeam team;
	private final Object2IntMap<ScoreObjective> objectives = new Object2IntOpenHashMap<>();

	private final DisguiseType disguise;

	private PlayerSnapshot(ServerPlayerEntity player) {
		this.gameType = player.gameMode.getGameModeForPlayer();
		this.dimension = player.level.dimension();
		this.pos = player.blockPosition();

		this.playerData = new CompoundNBT();
		player.addAdditionalSaveData(this.playerData);
		this.playerData.remove("playerGameType");

		ServerScoreboard scoreboard = player.getLevel().getScoreboard();
		this.team = scoreboard.getPlayersTeam(player.getScoreboardName());
		for (Map.Entry<ScoreObjective, Score> entry : scoreboard.getPlayerScores(player.getScoreboardName()).entrySet()) {
			this.objectives.put(entry.getKey(), entry.getValue().getScore());
		}

		this.disguise = PlayerDisguise.getDisguiseType(player);
	}

	public static PlayerSnapshot takeAndClear(ServerPlayerEntity player) {
		PlayerSnapshot snapshot = new PlayerSnapshot(player);
		clearPlayer(player);
		return snapshot;
	}

	public static void clearPlayer(ServerPlayerEntity player) {
		player.inventory.clearContent();
		player.setHealth(player.getMaxHealth());

		player.removeAllEffects();
		player.setGlowing(false);
		player.setArrowCount(0);
		player.fallDistance = 0.0F;

		CompoundNBT foodTag = new CompoundNBT();
		new FoodStats().addAdditionalSaveData(foodTag);
		player.getFoodData().readAdditionalSaveData(foodTag);

		ServerPlayerDisguises.clear(player);

		ServerScoreboard scoreboard = player.getLevel().getScoreboard();
		scoreboard.removePlayerFromTeam(player.getScoreboardName());

		Map<ScoreObjective, Score> objectives = scoreboard.getPlayerScores(player.getScoreboardName());
		for (ScoreObjective objective : new ArrayList<>(objectives.keySet())) {
			scoreboard.resetPlayerScore(player.getScoreboardName(), objective);
		}
	}

	/**
	 * Resets the player back to their previous state when this cache
	 * was created.
	 *
	 * @param player The player being reset.
	 */
	public void restore(ServerPlayerEntity player) {
		clearPlayer(player);

		player.readAdditionalSaveData(this.playerData);
		player.setGameMode(this.gameType);

		DimensionUtils.teleportPlayerNoPortal(player, this.dimension, this.pos);

		PlayerDisguise.get(player).ifPresent(disguise -> disguise.setDisguise(this.disguise));

		ServerScoreboard scoreboard = player.getLevel().getScoreboard();

		if (this.team != null) {
			scoreboard.addPlayerToTeam(player.getScoreboardName(), this.team);
		}

		for (Object2IntMap.Entry<ScoreObjective> entry : this.objectives.object2IntEntrySet()) {
			Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), entry.getKey());
			score.setScore(entry.getIntValue());
		}

		player.refreshContainer(player.containerMenu);
	}
}
