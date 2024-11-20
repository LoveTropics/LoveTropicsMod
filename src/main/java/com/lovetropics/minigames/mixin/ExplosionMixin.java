package com.lovetropics.minigames.mixin;

import com.lovetropics.minigames.common.util.duck.ExtendedExplosion;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Explosion.class)
public class ExplosionMixin implements ExtendedExplosion {
	@Mutable
	@Shadow
	@Final
	private Holder<SoundEvent> explosionSound;

	@Override
	public void ltminigames$setSound(Holder<SoundEvent> sound) {
		explosionSound = sound;
	}
}
