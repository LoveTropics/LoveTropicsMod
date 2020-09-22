package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.GameType;

public class SetGameTypesBehavior implements IMinigameBehavior {
	private final GameType participantGameType;
	private final GameType spectatorGameType;

	public SetGameTypesBehavior(GameType participantGameType, GameType spectatorGameType) {
		this.participantGameType = participantGameType;
		this.spectatorGameType = spectatorGameType;
	}

	@Override
	public void onPlayerJoin(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		player.setGameType(role == PlayerRole.PARTICIPANT ? participantGameType : spectatorGameType);
	}
}
