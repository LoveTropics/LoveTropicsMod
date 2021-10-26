package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerPlacement;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;

import java.util.ArrayList;
import java.util.List;

public final class PlaceByDeathOrderBehavior implements IGameBehavior {
	public static final Codec<PlaceByDeathOrderBehavior> CODEC = Codec.unit(PlaceByDeathOrderBehavior::new);

	private final List<PlayerKey> deathOrder = new ArrayList<>();

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
		events.listen(GamePlayerEvents.LEAVE, this::onPlayerLeave);
		events.listen(GamePhaseEvents.FINISH, () -> onFinish(game));
	}

	private ActionResultType onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		PlayerKey playerKey = PlayerKey.from(player);
		if (!deathOrder.contains(playerKey)) {
			deathOrder.add(playerKey);
		}
		return ActionResultType.PASS;
	}

	private void onPlayerLeave(ServerPlayerEntity player) {
		PlayerKey playerKey = PlayerKey.from(player);
		if (!deathOrder.contains(playerKey)) {
			deathOrder.add(playerKey);
		}
	}

	private void onFinish(IGamePhase game) {
		PlayerPlacement.fromDeathOrder(game, deathOrder).placeInto(StatisticKey.PLACEMENT);
	}
}
