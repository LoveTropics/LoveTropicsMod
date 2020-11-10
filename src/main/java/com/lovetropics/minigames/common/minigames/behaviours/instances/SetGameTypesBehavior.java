package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.GameType;

public class SetGameTypesBehavior implements IMinigameBehavior {
	private final GameType participantGameType;
	private final GameType spectatorGameType;

	public SetGameTypesBehavior(GameType participantGameType, GameType spectatorGameType) {
		this.participantGameType = participantGameType;
		this.spectatorGameType = spectatorGameType;
	}

	public static <T> SetGameTypesBehavior parse(Dynamic<T> root) {
		GameType participant = GameType.getByName(root.get("participant").asString(""));
		GameType spectator = GameType.getByName(root.get("spectator").asString(""));
		return new SetGameTypesBehavior(participant, spectator);
	}

	@Override
	public void onPlayerJoin(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		applyToPlayer(player, role);
	}

	@Override
	public void onPlayerChangeRole(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
		applyToPlayer(player, role);
	}

	private void applyToPlayer(ServerPlayerEntity player, PlayerRole role) {
		player.setGameType(role == PlayerRole.PARTICIPANT ? participantGameType : spectatorGameType);
	}
}
