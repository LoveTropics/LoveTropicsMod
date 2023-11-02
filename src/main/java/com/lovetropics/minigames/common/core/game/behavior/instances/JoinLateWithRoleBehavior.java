package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerRoleSelections;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record JoinLateWithRoleBehavior(PlayerRole role) implements IGameBehavior {
	public static final MapCodec<JoinLateWithRoleBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			PlayerRole.CODEC.fieldOf("role").forGetter(c -> c.role)
	).apply(i, JoinLateWithRoleBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		events.listen(GamePlayerEvents.JOIN, player -> {
			final PlayerRoleSelections roleSelections = game.getLobby().getPlayers().getRoleSelections();
			if (roleSelections.getSelectedRoleFor(player) == PlayerRole.SPECTATOR) {
				game.setPlayerRole(player, PlayerRole.SPECTATOR);
			} else {
				game.setPlayerRole(player, role);
			}
		});
	}
}
