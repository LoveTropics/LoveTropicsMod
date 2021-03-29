package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.Optional;

public class EffectPackageBehavior extends DonationPackageBehavior
{
	public static final Codec<EffectPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				DonationPackageData.CODEC.forGetter(c -> c.data),
				StatusEffect.CODEC.listOf().fieldOf("effects").forGetter(c -> c.effects)
		).apply(instance, EffectPackageBehavior::new);
	});

	private final List<StatusEffect> effects;

	public EffectPackageBehavior(final DonationPackageData data, final List<StatusEffect> effects) {
		super(data);

		this.effects = effects;
	}

	@Override
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		effects.forEach(effect -> effect.applyToPlayer(player));
	}

	public static class StatusEffect {
		public static final Codec<StatusEffect> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					ResourceLocation.CODEC.fieldOf("type").forGetter(c -> c.type),
					Codec.INT.fieldOf("seconds").forGetter(c -> c.seconds),
					Codec.INT.fieldOf("amplifier").forGetter(c -> c.amplifier),
					Codec.BOOL.fieldOf("hide_particles").forGetter(c -> c.hideParticles)
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

		public int getSeconds()
		{
			return seconds;
		}

		public int getAmplifier()
		{
			return amplifier;
		}

		public boolean hideParticles()
		{
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
