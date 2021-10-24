package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SwapPlayersPackageBehavior implements IGameBehavior {
	public static final Codec<SwapPlayersPackageBehavior> CODEC = Codec.unit(SwapPlayersPackageBehavior::new);

	private int swapCountdown;

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePackageEvents.APPLY_PACKAGE, (player, sendingPlayer) -> {
			if (swapCountdown <= 0) {
				swapCountdown = 20;
				return true;
			} else {
				return false;
			}
		});
		events.listen(GamePhaseEvents.TICK, () -> tick(game));
	}

	private void tick(IGamePhase game) {
		if (swapCountdown <= 0) return;

		if (--swapCountdown <= 0) {
			List<ServerPlayerEntity> players = Lists.newArrayList(game.getParticipants());
			Collections.shuffle(players);

			List<Vector3d> playerPositions = players.stream()
					.map(Entity::getPositionVec)
					.collect(Collectors.toList());

			for (int i = 0; i < players.size(); i++) {
				final ServerPlayerEntity player = players.get(i);
				final Vector3d teleportTo = playerPositions.get((i + 1) % playerPositions.size());
				player.setPositionAndUpdate(teleportTo.x, teleportTo.y, teleportTo.z);
			}
		}
	}
}
