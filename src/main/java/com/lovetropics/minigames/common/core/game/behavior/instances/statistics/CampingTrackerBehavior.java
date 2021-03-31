package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Map;
import java.util.UUID;

public final class CampingTrackerBehavior implements IGameBehavior {
	public static final Codec<CampingTrackerBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				TriggerAfterConfig.CODEC.optionalFieldOf("trigger", TriggerAfterConfig.EMPTY).forGetter(c -> c.trigger),
				Codec.LONG.optionalFieldOf("camp_time_threshold", 20L * 8).forGetter(c -> c.campTimeThreshold),
				Codec.DOUBLE.optionalFieldOf("camp_movement_threshold", 8.0).forGetter(c -> c.campMovementThreshold)
		).apply(instance, CampingTrackerBehavior::new);
	});

	private static final long CAMP_TEST_INTERVAL = 20;

	private final TriggerAfterConfig trigger;
	private final long campTimeThreshold;
	private final double campMovementThreshold;

	private final Map<UUID, CampingTracker> campingTrackers = new Object2ObjectOpenHashMap<>();

	public CampingTrackerBehavior(TriggerAfterConfig trigger, long campTimeThreshold, double campMovementThreshold) {
		this.trigger = trigger;
		this.campTimeThreshold = campTimeThreshold;
		this.campMovementThreshold = campMovementThreshold;
	}

	@Override
	public void register(IGameInstance game, EventRegistrar events) {
		trigger.awaitThen(events, () -> {
			events.listen(GameLifecycleEvents.TICK, this::tick);
			events.listen(GamePlayerEvents.LEAVE, this::onPlayerLeave);
			events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
		});
	}

	private void tick(IGameInstance game) {
		long ticks = game.ticks();
		if (ticks % CAMP_TEST_INTERVAL == 0) {
			testForCamping(game, ticks);
		}
	}

	private void testForCamping(IGameInstance game, long time) {
		GameStatistics statistics = game.getStatistics();

		for (ServerPlayerEntity player : game.getParticipants()) {
			CampingTracker tracker = getCampingTracker(player);

			Vector3d currentPosition = player.getPositionVec();
			if (tracker.camping) {
				int campingTime = tracker.trackCamping(currentPosition, time);
				if (campingTime > 0) {
					statistics.forPlayer(player).withDefault(StatisticKey.TIME_CAMPING, () -> 0)
							.apply(current -> current + campingTime);
				}
			} else {
				tracker.trackNotCamping(currentPosition, time);
			}
		}
	}

	private CampingTracker getCampingTracker(ServerPlayerEntity player) {
		return campingTrackers.computeIfAbsent(player.getUniqueID(), i -> new CampingTracker());
	}

	private ActionResultType onPlayerDeath(IGameInstance game, ServerPlayerEntity player, DamageSource source) {
		campingTrackers.remove(player.getUniqueID());
		return ActionResultType.PASS;
	}

	private void onPlayerLeave(IGameInstance game, ServerPlayerEntity player) {
		campingTrackers.remove(player.getUniqueID());
	}

	class CampingTracker {
		boolean camping;

		Vector3d lastPosition;
		long lastTrackTime;

		void trackNotCamping(Vector3d currentPosition, long time) {
			if (lastPosition == null) {
				lastPosition = currentPosition;
				lastTrackTime = time;
			}

			if (time - lastTrackTime > campTimeThreshold) {
				double movement = currentPosition.distanceTo(lastPosition);

				lastTrackTime = time;
				lastPosition = currentPosition;

				if (movement < campMovementThreshold) {
					camping = true;
				}
			}
		}

		int trackCamping(Vector3d currentPosition, long time) {
			double movement = currentPosition.distanceTo(lastPosition);
			if (movement > campMovementThreshold) {
				camping = false;
				lastPosition = currentPosition;
			}

			long duration = time - lastTrackTime;
			if (duration >= 20) {
				long seconds = duration / 20;
				lastTrackTime += seconds * 20;
				return (int) seconds;
			}

			return 0;
		}
	}
}
