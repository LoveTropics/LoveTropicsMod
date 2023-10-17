package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;

public record GiveEffectAction(List<MobEffectInstance> effects) implements IGameBehavior {
	public static final MapCodec<GiveEffectAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MoreCodecs.EFFECT_INSTANCE.listOf().fieldOf("effects").forGetter(c -> c.effects)
	).apply(i, GiveEffectAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			for (MobEffectInstance effect : effects) {
				player.addEffect(new MobEffectInstance(effect));
			}
			return true;
		});
	}
}
