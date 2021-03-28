package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.MoreCodecs;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.GameType;

public class SetGameTypesBehavior implements IMinigameBehavior {
	public static final Codec<SetGameTypesBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.GAME_TYPE.fieldOf("participant").forGetter(c -> c.participantGameType),
				MoreCodecs.GAME_TYPE.fieldOf("spectator").forGetter(c -> c.spectatorGameType)
		).apply(instance, SetGameTypesBehavior::new);
	});

	private final GameType participantGameType;
	private final GameType spectatorGameType;

	public SetGameTypesBehavior(GameType participantGameType, GameType spectatorGameType) {
		this.participantGameType = participantGameType;
		this.spectatorGameType = spectatorGameType;
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
