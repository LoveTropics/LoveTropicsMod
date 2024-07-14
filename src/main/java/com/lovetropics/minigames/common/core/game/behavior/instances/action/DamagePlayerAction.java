package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

import java.util.Optional;

public record DamagePlayerAction(Optional<Holder<DamageType>> source, float amount) implements IGameBehavior {
	public static final MapCodec<DamagePlayerAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			DamageType.CODEC.optionalFieldOf("source").forGetter(DamagePlayerAction::source),
			Codec.FLOAT.fieldOf("amount").forGetter(DamagePlayerAction::amount)
	).apply(i, DamagePlayerAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			player.hurt(
					source.map(DamageSource::new)
							.orElseGet(player.damageSources()::generic),
					amount
			);
			return true;
		});
	}
}
