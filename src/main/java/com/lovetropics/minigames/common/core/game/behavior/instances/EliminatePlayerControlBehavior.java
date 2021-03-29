package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.ControlCommand;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class EliminatePlayerControlBehavior implements IGameBehavior {
	public static final Codec<EliminatePlayerControlBehavior> CODEC = Codec.unit(EliminatePlayerControlBehavior::new);

	@Override
	public void onConstruct(IGameInstance minigame) {
		minigame.addControlCommand("eliminate", ControlCommand.forAdmins(source -> {
			ServerPlayerEntity player = source.asPlayer();
			if (!minigame.getSpectators().contains(player.getUniqueID())) {
				minigame.addPlayer(player, PlayerRole.SPECTATOR);
				player.setHealth(20.0F);
			}
		}));
	}
}
