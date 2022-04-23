package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantItemType;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;

import java.util.List;

public final class BbGivePlantsBehavior implements IGameBehavior {
	public static final Codec<BbGivePlantsBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				PlantConfig.CODEC.listOf().fieldOf("plants").forGetter(c -> c.plants)
		).apply(instance, BbGivePlantsBehavior::new);
	});

	private final List<PlantConfig> plants;

	public BbGivePlantsBehavior(List<PlantConfig> plants) {
		this.plants = plants;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(BbEvents.ASSIGN_PLOT, (player, plot) -> {
			for (PlantConfig plant : this.plants) {
				ItemStack stack = plant.create(game);
				if (!stack.isEmpty()) {
					player.addItem(stack);
				}
			}
		});
	}

	static final class PlantConfig {
		public static final Codec<PlantConfig> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					PlantItemType.CODEC.fieldOf("item").forGetter(c -> c.item),
					Codec.INT.optionalFieldOf("count", 1).forGetter(c -> c.count)
			).apply(instance, PlantConfig::new);
		});

		final PlantItemType item;
		final int count;

		PlantConfig(PlantItemType item, int count) {
			this.item = item;
			this.count = count;
		}

		ItemStack create(IGamePhase game) {
			ItemStack stack = game.invoker(BbEvents.CREATE_PLANT_ITEM).createPlantItem(item);
			if (!stack.isEmpty()) {
				stack.setCount(count);
			}
			return stack;
		}
	}
}
