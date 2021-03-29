package com.lovetropics.minigames.common.core.game.statistics;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.ArrayList;
import java.util.List;

public final class PlaceByDeathOrderBehavior implements IGameBehavior {
	public static final Codec<PlaceByDeathOrderBehavior> CODEC = Codec.unit(PlaceByDeathOrderBehavior::new);

	private final List<PlayerKey> deathOrder = new ArrayList<>();

	@Override
	public void onPlayerDeath(IGameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		PlayerKey playerKey = PlayerKey.from(player);
		if (!deathOrder.contains(playerKey)) {
			deathOrder.add(playerKey);
		}
	}

	@Override
	public void onPlayerLeave(IGameInstance minigame, ServerPlayerEntity player) {
		PlayerKey playerKey = PlayerKey.from(player);
		if (!deathOrder.contains(playerKey)) {
			deathOrder.add(playerKey);
		}
	}

	@Override
	public void onFinish(IGameInstance minigame) {
		PlayerPlacement.fromDeathOrder(minigame, deathOrder).placeInto(StatisticKey.PLACEMENT);
	}
}
