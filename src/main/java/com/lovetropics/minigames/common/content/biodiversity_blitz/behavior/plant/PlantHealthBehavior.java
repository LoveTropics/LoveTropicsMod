package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantType;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state.PlantHealth;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PlantHealthBehavior implements IGameBehavior {
	public static final Codec<PlantHealthBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("health").forGetter(b -> b.health)
	).apply(instance, PlantHealthBehavior::new));

	private final int health;

	public PlantHealthBehavior(int health) {
		this.health = health;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(BbPlantEvents.ADD, (player, plot, plant) -> {
			plant.state().put(PlantHealth.KEY, new PlantHealth(this.health));
		});

		events.listen(BbPlantEvents.TICK, (player, plot, plants) -> {
			ServerWorld world = game.getWorld();

			List<Plant> decayedPlants = new ArrayList<>();

			for (Plant plant : plants) {
				PlantHealth health = plant.state(PlantHealth.KEY);
				if (health == null) {
					continue;
				}

				if (health.isDead()) {
					plant.spawnPoof(world);
					decayedPlants.add(plant);
				}
			}

			decayedPlants.forEach(plant -> game.invoker(BbEvents.BREAK_PLANT).breakPlant(player, plot, plant));
		});
	}
}
