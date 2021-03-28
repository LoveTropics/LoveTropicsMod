package com.lovetropics.minigames.common.minigames.behaviours.instances.statistics;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import com.lovetropics.minigames.common.minigames.statistics.PlayerPlacement;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.ArrayList;
import java.util.List;

public final class PlaceByDeathOrderBehavior implements IMinigameBehavior {
	public static final Codec<PlaceByDeathOrderBehavior> CODEC = Codec.unit(PlaceByDeathOrderBehavior::new);

	private final List<PlayerKey> deathOrder = new ArrayList<>();

	@Override
	public void onPlayerDeath(IMinigameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		PlayerKey playerKey = PlayerKey.from(player);
		if (!deathOrder.contains(playerKey)) {
			deathOrder.add(playerKey);
		}
	}

	@Override
	public void onPlayerLeave(IMinigameInstance minigame, ServerPlayerEntity player) {
		PlayerKey playerKey = PlayerKey.from(player);
		if (!deathOrder.contains(playerKey)) {
			deathOrder.add(playerKey);
		}
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		PlayerPlacement.fromDeathOrder(minigame, deathOrder).placeInto(StatisticKey.PLACEMENT);
	}
}
