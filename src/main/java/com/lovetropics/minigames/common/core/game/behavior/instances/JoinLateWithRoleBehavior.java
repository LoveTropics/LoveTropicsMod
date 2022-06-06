package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record JoinLateWithRoleBehavior(PlayerRole role) implements IGameBehavior {
	public static final Codec<JoinLateWithRoleBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			PlayerRole.CODEC.fieldOf("role").forGetter(c -> c.role)
	).apply(i, JoinLateWithRoleBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.JOIN, player -> game.setPlayerRole(player, role));
	}
}
