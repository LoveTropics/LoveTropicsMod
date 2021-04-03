package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;

public final class DamageTrackerBehavior implements IGameBehavior {
	public static final Codec<DamageTrackerBehavior> CODEC = Codec.unit(DamageTrackerBehavior::new);

	@Override
	public void register(IActiveGame registerGame, EventRegistrar events) {
		events.listen(GamePlayerEvents.DAMAGE, (game, player, source, damageAmount) -> {
			GameStatistics statistics = game.getStatistics();

			statistics.forPlayer(player)
					.withDefault(StatisticKey.DAMAGE_TAKEN, () -> 0.0F)
					.apply(total -> total + damageAmount);

			Entity attacker = source.getTrueSource();
			if (attacker instanceof ServerPlayerEntity) {
				statistics.forPlayer((PlayerEntity) attacker)
						.withDefault(StatisticKey.DAMAGE_DEALT, () -> 0.0F)
						.apply(total -> total + damageAmount);
			}

			return ActionResultType.PASS;
		});
	}
}
