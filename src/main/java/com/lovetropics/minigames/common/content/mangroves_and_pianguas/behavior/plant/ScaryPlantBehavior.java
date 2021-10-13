package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.Plant;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.state.PlantHealth;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Random;

// TODO: people can perpetually replace jack o' lanterns to reset their timer. Is there a better way to do this?
public final class ScaryPlantBehavior implements IGameBehavior {
	public static final Codec<ScaryPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.DOUBLE.fieldOf("radius").forGetter(c -> c.radius),
				Codec.INT.fieldOf("push_cost").forGetter(c -> c.pushCost)
		).apply(instance, ScaryPlantBehavior::new);
	});

	private final double radius;

	private final int pushCost;

	public ScaryPlantBehavior(double radius, int pushCost) {
		this.radius = radius;
		this.pushCost = pushCost;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(MpEvents.TICK_PLANTS, (player, plot, plants) -> {
			long ticks = game.ticks();
			if (ticks % 5 != 0) return;

			ServerWorld world = game.getWorld();
			Random random = world.rand;

			for (Plant plant : plants) {
				AxisAlignedBB bounds = plant.coverage().asBounds();
				AxisAlignedBB pushBounds = bounds.grow(this.radius);
				List<MobEntity> entities = world.getEntitiesWithinAABB(MobEntity.class, pushBounds, entity -> !(entity instanceof VillagerEntity));

				Vector3d pushFrom = bounds.getCenter();

				for (MobEntity entity : entities) {
					Vector3d entityPos = entity.getPositionVec();

					// Scaled so that closer values are higher, with a max of 10
					double dist = 2.0 / (0.1 + entityPos.distanceTo(pushFrom));

					// Angle between entity and center of lantern
					double theta = Math.atan2(entityPos.z - pushFrom.z, entityPos.x - pushFrom.x);

					// zoooooom
					entity.addVelocity(dist * Math.cos(theta), 0.25, dist * Math.sin(theta));

					// Prevent mobs from flying to the moon due to too much motion
					Vector3d motion = entity.getMotion();
					entity.setMotion(Math.min(motion.x, 10), Math.min(motion.y, 0.25), Math.min(motion.z, 10));

					// Make it so that using a jack o lantern a lot will reduce alive time
					if (random.nextInt(6) == 0) {
						PlantHealth health = plant.state(PlantHealth.KEY);
						if (health != null) {
							health.decrement(this.pushCost);
						}
					}
				}
			}
		});
	}
}
