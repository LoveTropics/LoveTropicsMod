package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SwapPlayersPackageBehavior implements IGameBehavior {
	public static final Codec<SwapPlayersPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.DOUBLE.optionalFieldOf("distance_threshold", Double.MAX_VALUE).forGetter(c -> c.distanceThreshold)
		).apply(instance, SwapPlayersPackageBehavior::new);
	});

	private final double distanceThreshold;

	public SwapPlayersPackageBehavior(double distanceThreshold) {
		this.distanceThreshold = distanceThreshold;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePackageEvents.APPLY_PACKAGE, (player, sendingPlayer) -> {
			if (this.distanceThreshold == Double.MAX_VALUE) {
				this.shufflePlayers(game);
			} else {
				this.swapNearbyPlayers(game);
			}
			return true;
		});
	}

	private void shufflePlayers(IGamePhase game) {
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

	private void swapNearbyPlayers(IGamePhase game) {
		List<ServerPlayerEntity> players = Lists.newArrayList(game.getParticipants());
		Collections.shuffle(players);

		List<Vector3d> playerPositions = players.stream()
				.map(Entity::getPositionVec)
				.collect(Collectors.toList());

		double distanceThreshold2 = distanceThreshold * distanceThreshold;

		for (ServerPlayerEntity player : players) {
			Vector3d closestPos = null;
			double closestDistance2 = Double.MAX_VALUE;

			for (Vector3d otherPos : playerPositions) {
				double distance2 = player.getPositionVec().squareDistanceTo(otherPos);
				if (distance2 > 0.01 && distance2 < distanceThreshold2) {
					if (distance2 < closestDistance2) {
						closestPos = otherPos;
						closestDistance2 = distance2;
					}
				}
			}

			if (closestPos != null) {
				player.setPositionAndUpdate(closestPos.x, closestPos.y, closestPos.z);
			}
		}
	}
}
