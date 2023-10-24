package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public record PlaySoundAction(SoundEvent sound, float volume, float pitch, SoundSource source) implements IGameBehavior {
	public static final MapCodec<PlaySoundAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ForgeRegistries.SOUND_EVENTS.getCodec().optionalFieldOf("sound", SoundEvents.ARROW_HIT_PLAYER).forGetter(PlaySoundAction::sound),
			Codec.FLOAT.optionalFieldOf("volume", 1.0f).forGetter(PlaySoundAction::volume),
			Codec.FLOAT.optionalFieldOf("pitch", 1.0f).forGetter(PlaySoundAction::pitch),
			MoreCodecs.stringVariants(SoundSource.values(), SoundSource::getName).optionalFieldOf("source", SoundSource.AMBIENT).forGetter(PlaySoundAction::source)
	).apply(i, PlaySoundAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target) -> {
			target.playNotifySound(sound, source, volume, pitch);
			return true;
		});
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return GameBehaviorTypes.PLAY_SOUND;
	}
}
