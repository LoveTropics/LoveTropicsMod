package com.lovetropics.minigames.common.minigames.behaviours.instances.statistics;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public final class DamageTrackerBehavior implements IMinigameBehavior {
	public static final Codec<DamageTrackerBehavior> CODEC = Codec.unit(DamageTrackerBehavior::new);

	@Override
	public void onPlayerHurt(IMinigameInstance minigame, LivingHurtEvent event) {
		MinigameStatistics statistics = minigame.getStatistics();

		DamageSource source = event.getSource();
		float damageAmount = event.getAmount();

		LivingEntity entity = event.getEntityLiving();
		if (entity instanceof ServerPlayerEntity) {
			statistics.forPlayer((PlayerEntity) entity)
					.withDefault(StatisticKey.DAMAGE_TAKEN, () -> 0.0F)
					.apply(total -> total + damageAmount);
		}

		Entity attacker = source.getTrueSource();
		if (attacker instanceof ServerPlayerEntity) {
			statistics.forPlayer((PlayerEntity) attacker)
					.withDefault(StatisticKey.DAMAGE_DEALT, () -> 0.0F)
					.apply(total -> total + damageAmount);
		}
	}
}
