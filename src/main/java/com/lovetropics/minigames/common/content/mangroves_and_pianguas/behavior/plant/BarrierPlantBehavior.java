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
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public final class BarrierPlantBehavior implements IGameBehavior {
	public static final Codec<BarrierPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.DOUBLE.fieldOf("radius").forGetter(c -> c.radius)
	).apply(instance, BarrierPlantBehavior::new));

	private final double radius;

	public BarrierPlantBehavior(double radius) {
		this.radius = radius;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(MpEvents.TICK_PLANTS, (player, plot, plants) -> {
			long ticks = game.ticks();
			if (ticks % 20 != 0) return;

			ServerWorld world = game.getWorld();

			for (Plant plant : plants) {
				PlantHealth health = plant.state(PlantHealth.KEY);
				if (health == null) {
					continue;
				}

				AxisAlignedBB bounds = plant.coverage().asBounds();
				AxisAlignedBB damageBounds = bounds.grow(1.0, 5.0, 1.0);

				List<MobEntity> entities = world.getEntitiesWithinAABB(MobEntity.class, damageBounds, entity -> !(entity instanceof VillagerEntity));
				if (!entities.isEmpty()) {
					health.decrement(entities.size());
				}
			}
		});
	}
}
