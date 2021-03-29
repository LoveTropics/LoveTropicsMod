package com.lovetropics.minigames.common.minigames.behaviours.instances.statistics;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CampingTrackerBehavior implements IMinigameBehavior {
	public static final Codec<CampingTrackerBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.optionalFieldOf("after_phase").forGetter(c -> Optional.ofNullable(c.afterPhase)),
				Codec.LONG.optionalFieldOf("camp_time_threshold", 20L * 8).forGetter(c -> c.campTimeThreshold),
				Codec.DOUBLE.optionalFieldOf("camp_movement_threshold", 8.0).forGetter(c -> c.campMovementThreshold)
		).apply(instance, CampingTrackerBehavior::new);
	});

	private static final long CAMP_TEST_INTERVAL = 20;

	private final String afterPhase;
	private final long campTimeThreshold;
	private final double campMovementThreshold;

	private long startTime;

	private final Map<UUID, CampingTracker> campingTrackers = new Object2ObjectOpenHashMap<>();

	public CampingTrackerBehavior(Optional<String> afterPhase, long campTimeThreshold, double campMovementThreshold) {
		this.afterPhase = afterPhase.orElse(null);
		this.campTimeThreshold = campTimeThreshold;
		this.campMovementThreshold = campMovementThreshold;
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, ServerWorld world) {
		if (startTime == 0 && afterPhase != null) {
			minigame.getOneBehavior(MinigameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
				if (!phases.getCurrentPhase().is(afterPhase)) {
					startTime = minigame.ticks();
				}
			});
		}

		if (startTime != 0) {
			long ticks = minigame.ticks();
			if (ticks % CAMP_TEST_INTERVAL == 0) {
				testForCamping(minigame, ticks);
			}
		}
	}

	private void testForCamping(IMinigameInstance minigame, long time) {
		MinigameStatistics statistics = minigame.getStatistics();

		for (ServerPlayerEntity player : minigame.getParticipants()) {
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

	@Override
	public void onPlayerDeath(IMinigameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		campingTrackers.remove(player.getUniqueID());
	}

	@Override
	public void onPlayerLeave(IMinigameInstance minigame, ServerPlayerEntity player) {
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
