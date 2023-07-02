package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.ForgeRegistries;

public record PlaySoundAction(SoundEvent sound, float volume, float pitch) implements IGameBehavior {
	public static final Codec<PlaySoundAction> CODEC = RecordCodecBuilder.create(i -> i.group(
			ForgeRegistries.SOUND_EVENTS.getCodec().optionalFieldOf("sound", SoundEvents.ARROW_HIT_PLAYER).forGetter(PlaySoundAction::sound),
			Codec.FLOAT.optionalFieldOf("volume", 1.0f).forGetter(PlaySoundAction::volume),
			Codec.FLOAT.optionalFieldOf("pitch", 1.0f).forGetter(PlaySoundAction::pitch)
	).apply(i, PlaySoundAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target) -> {
			target.playNotifySound(sound, SoundSource.AMBIENT, volume, pitch);
			return true;
		});
	}
}
