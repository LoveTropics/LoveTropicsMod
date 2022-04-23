package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;

public class EffectPackageBehavior implements IGameBehavior {
	public static final Codec<EffectPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.EFFECT_INSTANCE.listOf().fieldOf("effects").forGetter(c -> c.effects)
		).apply(instance, EffectPackageBehavior::new);
	});

	private final List<MobEffectInstance> effects;

	public EffectPackageBehavior(List<MobEffectInstance> effects) {
		this.effects = effects;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GamePackageEvents.APPLY_PACKAGE_TO_PLAYER, (player, sendingPlayer) -> {
			for (MobEffectInstance effect : effects) {
				player.addEffect(new MobEffectInstance(effect));
			}
			return true;
		});
	}
}
