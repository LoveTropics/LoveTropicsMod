package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SetPlayerRoleAction(PlayerRole role) implements IGameBehavior {
	public static final MapCodec<SetPlayerRoleAction> CODEC = PlayerRole.CODEC.fieldOf("role").xmap(SetPlayerRoleAction::new, SetPlayerRoleAction::role);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target) -> {
			if (game.getRoleFor(target) != role) {
				game.setPlayerRole(target, role);
				target.setHealth(20.0F);
				return true;
			}
			return false;
		});
	}
}
