package com.lovetropics.minigames.common.minigames.behaviours.instances.statistics;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.Map;
import java.util.UUID;

public final class CampingTrackerBehavior implements IMinigameBehavior {
	private static final long CAMP_TEST_INTERVAL = 20;

	private final String afterPhase;
	private final long campTimeThreshold;
	private final double campMovementThreshold;

	private long startTime;

	private final Map<UUID, CampingTracker> campingTrackers = new Object2ObjectOpenHashMap<>();

	public CampingTrackerBehavior(String afterPhase, long campTimeThreshold, double campMovementThreshold) {
		this.afterPhase = afterPhase;
		this.campTimeThreshold = campTimeThreshold;
		this.campMovementThreshold = campMovementThreshold;
	}

	public static <T> CampingTrackerBehavior parse(Dynamic<T> root) {
		String afterPhase = root.get("after_phase").asString(null);
		long campTimeThreshold = root.get("camp_time_threshold").asLong(20 * 8);
		double campMovementThreshold = root.get("camp_movement_threshold").asDouble(8.0);
		return new CampingTrackerBehavior(afterPhase, campTimeThreshold, campMovementThreshold);
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, World world) {
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

			Vec3d currentPosition = player.getPositionVec();
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

		Vec3d lastPosition;
		long lastTrackTime;

		void trackNotCamping(Vec3d currentPosition, long time) {
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

		int trackCamping(Vec3d currentPosition, long time) {
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
