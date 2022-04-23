package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class FruitDropEntityBehavior implements IGameBehavior {
	
	@SuppressWarnings("deprecation")
	public static final Codec<FruitDropEntityBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("range").forGetter(o -> o.range),
				Codec.INT.fieldOf("interval").forGetter(o -> o.interval),
				Registry.BLOCK.fieldOf("fruit").forGetter(o -> o.fruit),
				Registry.ENTITY_TYPE.fieldOf("entity").forGetter(o -> o.entity)
			).apply(instance, FruitDropEntityBehavior::new));

	private final int range;
	private final int interval;
	private final Block fruit;
	private final EntityType<?> entity;

	private IGamePhase game;

	public FruitDropEntityBehavior(int range, int interval, Block fruit, EntityType<?> entity) {
		this.range = range;
		this.interval = interval;
		this.fruit = fruit;
		this.entity = entity;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		this.game = game;
		events.listen(BbPlantEvents.TICK, this::tick);
	}

	private void tick(ServerPlayerEntity player, Plot plot, List<Plant> plants) {
		if (game.ticks() % interval == 0) {
			for (Plant plant : plants) {
				updateCoconuts(player, plot, plant);
			}
		}
	}

	private void updateCoconuts(ServerPlayerEntity player, Plot plot, Plant plant) {
		plant.functionalCoverage().stream()
			.flatMap(bp -> IntStream.range(0, 4).mapToObj(Direction::from2DDataValue).map(bp::relative))
			.filter(bp -> player.level.getBlockState(bp).getBlock() == fruit)
			.filter(bp -> !player.level.getEntities(player, new AxisAlignedBB(bp).inflate(range - 1, 15, range - 1), (Predicate<? super Entity>) e -> (e instanceof BbMobEntity)).isEmpty())
			.forEach(bp -> {
				player.level.setBlockAndUpdate(bp, Blocks.AIR.defaultBlockState());
				player.level.addFreshEntity(entity.spawn(player.getLevel(), null, null, player, bp, SpawnReason.TRIGGERED, false, false));
			});
	}
}
