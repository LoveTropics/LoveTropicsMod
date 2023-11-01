package com.lovetropics.minigames.common.content.qottott.behavior;

import com.lovetropics.minigames.common.content.qottott.Qottott;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.function.Supplier;

public record PowerUpIndicatorBehavior(MobEffect effect, TemplatedText text) implements IGameBehavior {
	public static final MapCodec<PowerUpIndicatorBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("effect").forGetter(PowerUpIndicatorBehavior::effect),
			TemplatedText.CODEC.fieldOf("text").forGetter(PowerUpIndicatorBehavior::text)
	).apply(i, PowerUpIndicatorBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		events.listen(GamePlayerEvents.TICK, player -> {
			final MobEffectInstance instance = player.getEffect(effect);
			if (instance != null && instance.getDuration() % 5 == 0) {
				final int seconds = Mth.floorDiv(instance.getDuration(), SharedConstants.TICKS_PER_SECOND);
				player.sendSystemMessage(text.apply(Map.of("seconds", Component.literal(String.valueOf(seconds)))), true);
			}
		});
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return Qottott.POWER_UP_INDICATOR;
	}
}
