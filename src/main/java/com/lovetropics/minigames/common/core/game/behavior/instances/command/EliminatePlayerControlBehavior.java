package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.control.ControlCommand;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class EliminatePlayerControlBehavior implements IGameBehavior {
	public static final Codec<EliminatePlayerControlBehavior> CODEC = Codec.unit(EliminatePlayerControlBehavior::new);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		game.getControlCommands().add("eliminate", ControlCommand.forAdmins(source -> {
			ServerPlayerEntity player = source.getPlayerOrException();
			if (!game.getSpectators().contains(player)) {
				game.setPlayerRole(player, PlayerRole.SPECTATOR);
				player.setHealth(20.0F);
			}
		}));
	}
}
