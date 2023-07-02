package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DamagePlayerAction(float amount) implements IGameBehavior {
	public static final Codec<DamagePlayerAction> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.FLOAT.fieldOf("amount").forGetter(DamagePlayerAction::amount)
	).apply(i, DamagePlayerAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			// TODO: Support other damage sources
			player.hurt(player.damageSources().generic(), amount);
			return true;
		});
	}
}
