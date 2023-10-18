package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerPlacement;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;

import java.util.ArrayList;
import java.util.List;

public final class PlaceByDeathOrderBehavior implements IGameBehavior {
	public static final MapCodec<PlaceByDeathOrderBehavior> CODEC = MapCodec.unit(PlaceByDeathOrderBehavior::new);

	private final List<PlayerKey> deathOrder = new ArrayList<>();

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
		events.listen(GamePlayerEvents.LEAVE, this::onPlayerLeave);
		events.listen(GamePhaseEvents.FINISH, () -> onFinish(game));
	}

	private InteractionResult onPlayerDeath(ServerPlayer player, DamageSource source) {
		PlayerKey playerKey = PlayerKey.from(player);
		if (!deathOrder.contains(playerKey)) {
			deathOrder.add(playerKey);
		}
		return InteractionResult.PASS;
	}

	private void onPlayerLeave(ServerPlayer player) {
		PlayerKey playerKey = PlayerKey.from(player);
		if (!deathOrder.contains(playerKey)) {
			deathOrder.add(playerKey);
		}
	}

	private void onFinish(IGamePhase game) {
		PlayerPlacement.fromDeathOrder(game, deathOrder).placeInto(StatisticKey.PLACEMENT);
	}
}
