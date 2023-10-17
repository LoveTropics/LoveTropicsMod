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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class FruitDropEntityBehavior implements IGameBehavior {

	public static final MapCodec<FruitDropEntityBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.INT.fieldOf("range").forGetter(o -> o.range),
			Codec.INT.fieldOf("interval").forGetter(o -> o.interval),
			ForgeRegistries.BLOCKS.getCodec().fieldOf("fruit").forGetter(o -> o.fruit),
			ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("entity").forGetter(o -> o.entity)
	).apply(i, FruitDropEntityBehavior::new));

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

	private void tick(Collection<ServerPlayer> players, Plot plot, List<Plant> plants) {
		if (game.ticks() % interval == 0) {
			for (Plant plant : plants) {
				updateCoconuts(players, plot, plant);
			}
		}
	}

	private void updateCoconuts(Collection<ServerPlayer> players, Plot plot, Plant plant) {
		ServerPlayer player = players.iterator().next();

		plant.functionalCoverage().stream()
				.flatMap(bp -> IntStream.range(0, 4).mapToObj(Direction::from2DDataValue).map(bp::relative))
				.filter(bp -> player.level().getBlockState(bp).getBlock() == fruit)
				.filter(bp -> !player.level().getEntities(player, new AABB(bp).inflate(range - 1, 15, range - 1), (Predicate<? super Entity>) e -> (e instanceof BbMobEntity)).isEmpty())
				.forEach(bp -> {
					player.level().setBlockAndUpdate(bp, Blocks.AIR.defaultBlockState());
					Entity spawnedEntity = entity.spawn(player.serverLevel(), null, player, bp, MobSpawnType.TRIGGERED, false, false);
					if (spawnedEntity != null) {
						player.level().addFreshEntity(spawnedEntity);
					}
				});
	}
}
