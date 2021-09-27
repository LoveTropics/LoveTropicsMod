package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.Optional;

public class EffectPackageBehavior implements IGameBehavior {
	public static final Codec<EffectPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				StatusEffect.CODEC.listOf().fieldOf("effects").forGetter(c -> c.effects)
		).apply(instance, EffectPackageBehavior::new);
	});

	private final List<StatusEffect> effects;

	public EffectPackageBehavior(List<StatusEffect> effects) {
		this.effects = effects;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GamePackageEvents.APPLY_PACKAGE, (player, sendingPlayer) -> {
			for (StatusEffect effect : effects) {
				effect.applyToPlayer(player);
			}
		});
	}

	public static class StatusEffect {
		public static final Codec<StatusEffect> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					ResourceLocation.CODEC.fieldOf("type").forGetter(c -> c.type),
					Codec.INT.fieldOf("seconds").forGetter(c -> c.seconds),
					Codec.INT.fieldOf("amplifier").forGetter(c -> c.amplifier),
					Codec.BOOL.optionalFieldOf("hide_particles", false).forGetter(c -> c.hideParticles)
			).apply(instance, StatusEffect::new);
		});

		private final ResourceLocation type;
		private final int seconds;
		private final int amplifier;
		private final boolean hideParticles;

		public StatusEffect(final ResourceLocation type, final int seconds, final int amplifier, final boolean hideParticles) {
			this.type = type;
			this.seconds = seconds;
			this.amplifier = amplifier;
			this.hideParticles = hideParticles;
		}

		public ResourceLocation getType() {
			return type;
		}

		public int getSeconds() {
			return seconds;
		}

		public int getAmplifier() {
			return amplifier;
		}

		public boolean hideParticles() {
			return hideParticles;
		}

		public Optional<Effect> getEffect() {
			return Registry.EFFECTS.getOptional(type);
		}

		public void applyToPlayer(final ServerPlayerEntity player) {
			final Optional<Effect> effect = getEffect();

			effect.ifPresent(value -> player.addPotionEffect(new EffectInstance(value, seconds * 20, amplifier, false, !hideParticles)));
		}
	}
}
