package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.role.StreamHosts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ScalePlayerDamageBehavior(float factor, float hostFactor) implements IGameBehavior {
	private static final float NO_FACTOR = -1.0f;

	public static final Codec<ScalePlayerDamageBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.FLOAT.fieldOf("factor").forGetter(ScalePlayerDamageBehavior::factor),
			Codec.FLOAT.optionalFieldOf("host_factor", NO_FACTOR).forGetter(ScalePlayerDamageBehavior::hostFactor) // TODO: Don't hardcode it
	).apply(i, ScalePlayerDamageBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.DAMAGE_AMOUNT, (player, damageSource, amount, originalAmount) -> {
			if (amount <= 1.0f) {
				return amount;
			}
			float factor = this.factor;
			if (hostFactor != NO_FACTOR && StreamHosts.isHost(player)) {
				factor = hostFactor;
			}
			float newAmount = amount * factor;
			return Math.max(1.0f, newAmount);
		});
	}
}
