package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.Plant;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantType;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.state.PlantHealth;
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
			Codec.INT.fieldOf("initial").forGetter(c -> c.initial),
			Codec.INT.optionalFieldOf("decay_per_tick", 0).forGetter(c -> c.decayPerTick),
			PlantType.CODEC.optionalFieldOf("decay_into").forGetter(c -> Optional.ofNullable(c.decayInto))
	).apply(instance, PlantHealthBehavior::new));

	private final int initial;
	private final int decayPerTick;

	private final PlantType decayInto;

	public PlantHealthBehavior(int initial, int decayPerTick, Optional<PlantType> decayInto) {
		this.initial = initial;
		this.decayPerTick = decayPerTick;
		this.decayInto = decayInto.orElse(null);
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(MpEvents.ADD_PLANT, (player, plot, plant) -> {
			plant.state().put(PlantHealth.KEY, new PlantHealth(this.initial));
		});

		events.listen(MpEvents.TICK_PLANTS, (player, plot, plants) -> {
			ServerWorld world = game.getWorld();

			List<Plant> decayedPlants = new ArrayList<>();

			for (Plant plant : plants) {
				PlantHealth health = plant.state(PlantHealth.KEY);
				if (health == null) continue;

				health.decrement(decayPerTick);

				if (health.isDead()) {
					plant.spawnPoof(world);
					decayedPlants.add(plant);
				}
			}

			for (Plant plant : decayedPlants) {
				game.invoker(MpEvents.BREAK_PLANT).breakPlant(player, plot, plant);

				if (this.decayInto != null) {
					BlockPos origin = plant.coverage().getOrigin();
					game.invoker(MpEvents.PLACE_PLANT).placePlant(player, plot, origin, this.decayInto);
				}
			}
		});
	}
}
