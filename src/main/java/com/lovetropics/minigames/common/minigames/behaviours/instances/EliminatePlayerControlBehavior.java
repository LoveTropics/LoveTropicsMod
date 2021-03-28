package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.ControlCommand;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class EliminatePlayerControlBehavior implements IMinigameBehavior {
	public static final Codec<EliminatePlayerControlBehavior> CODEC = Codec.unit(EliminatePlayerControlBehavior::new);

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		minigame.addControlCommand("eliminate", ControlCommand.forAdmins(source -> {
			ServerPlayerEntity player = source.asPlayer();
			if (!minigame.getSpectators().contains(player.getUniqueID())) {
				minigame.addPlayer(player, PlayerRole.SPECTATOR);
				player.setHealth(20.0F);
			}
		}));
	}
}
