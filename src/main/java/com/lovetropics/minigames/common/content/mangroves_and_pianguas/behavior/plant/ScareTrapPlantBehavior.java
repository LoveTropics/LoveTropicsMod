package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpPlantEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.Plant;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.state.PlantHealth;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.state.PlantState;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

// TODO: people can perpetually replace jack o' lanterns to reset their timer. Is there a better way to do this?
public final class ScareTrapPlantBehavior implements IGameBehavior {
	public static final Codec<ScareTrapPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.DOUBLE.fieldOf("radius").forGetter(c -> c.radius),
			Codec.INT.fieldOf("push_cost").forGetter(c -> c.pushCost)
	).apply(instance, ScareTrapPlantBehavior::new));

	private final double radius;

	private final int pushCost;

	public ScareTrapPlantBehavior(double radius, int pushCost) {
		this.radius = radius;
		this.pushCost = pushCost;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(MpPlantEvents.ADD, (player, plot, plant) -> {
			plant.state().put(Trap.KEY, new Trap());
		});

		events.listen(MpPlantEvents.TICK, (player, plot, plants) -> {
			long ticks = game.ticks();
			if (ticks % 5 != 0) return;

			ServerWorld world = game.getWorld();

			for (Plant plant : plants) {
				Trap trap = plant.state(Trap.KEY);
				if (trap == null || !trap.ready) {
					continue;
				}

				if (this.tickTrap(world, plant)) {
					trap.ready = false;
				}
			}
		});
	}

	private boolean tickTrap(ServerWorld world, Plant plant) {
		AxisAlignedBB bounds = plant.coverage().asBounds();
		AxisAlignedBB pushBounds = bounds.grow(this.radius);
		List<MobEntity> entities = world.getEntitiesWithinAABB(MobEntity.class, pushBounds, entity -> !(entity instanceof VillagerEntity));

		if (!entities.isEmpty()) {
			this.triggerTrap(world, plant, bounds, entities);
			return true;
		}

		return false;
	}

	private void triggerTrap(ServerWorld world, Plant plant, AxisAlignedBB bounds, List<MobEntity> entities) {
		for (MobEntity entity : entities) {
			this.pushEntity(bounds.getCenter(), entity);
		}

		world.playSound(null, plant.coverage().getOrigin(), SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 1.0F, 1.0F);

		PlantHealth health = plant.state(PlantHealth.KEY);
		if (health != null) {
			health.decrement(this.pushCost * entities.size());
		}
	}

	private void pushEntity(Vector3d pushFrom, MobEntity entity) {
		Vector3d entityPos = entity.getPositionVec();

		// Scaled so that closer values are higher, with a max of 5
		double dist = 0.5 / (0.1 + entityPos.distanceTo(pushFrom));

		// Angle between entity and center of lantern
		double theta = Math.atan2(entityPos.z - pushFrom.z, entityPos.x - pushFrom.x);

		// zoooooom
		entity.addVelocity(dist * Math.cos(theta), 0.25, dist * Math.sin(theta));

		// Prevent mobs from flying to the moon due to too much motion
		Vector3d motion = entity.getMotion();
		entity.setMotion(Math.min(motion.x, 5), Math.min(motion.y, 0.25), Math.min(motion.z, 5));
	}

	private void setExtended(ServerWorld world, Plant plant) {

	}

	static final class Trap {
		static final PlantState.Key<Trap> KEY = PlantState.Key.create();

		boolean ready = true;
	}
}
