package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.PlotsState;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.*;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public final class MpPlantBehavior implements IGameBehavior {
	public static final Codec<MpPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlantType.CODEC.fieldOf("id").forGetter(c -> c.plantType),
			PlantPlacement.CODEC.fieldOf("place").forGetter(c -> c.place),
			Drops.CODEC.optionalFieldOf("drops").forGetter(c -> Optional.ofNullable(c.drops)),
			IGameBehavior.CODEC.listOf().optionalFieldOf("behaviors", Collections.emptyList()).forGetter(c -> c.behaviors)
	).apply(instance, MpPlantBehavior::new));

	private final PlantType plantType;
	private final PlantPlacement place;
	private final Drops drops;
	private final List<IGameBehavior> behaviors;

	private final GameEventListeners plantEvents = new GameEventListeners();

	private IGamePhase game;
	private PlotsState plots;

	public MpPlantBehavior(PlantType plantType, PlantPlacement place, Optional<Drops> drops, List<IGameBehavior> behaviors) {
		this.plantType = plantType;
		this.place = place;
		this.drops = drops.orElse(null);
		this.behaviors = behaviors;
	}

	private static boolean shouldPlantBehaviorHandle(GameEventType<?> type) {
		return type == MpEvents.TICK_PLANTS || type == MpEvents.ADD_PLANT;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;
		this.plots = game.getState().getOrThrow(PlotsState.KEY);

		events.listen(MpEvents.PLACE_PLANT, this::placePlant);
		events.listen(MpEvents.BREAK_PLANT, this::removePlant);

		events.listen(GamePlayerEvents.BREAK_BLOCK, this::onBreakBlock);

		events.listen(MpEvents.TICK_PLOT, this::onTickPlot);

		EventRegistrar plantEvents = events.redirect(MpPlantBehavior::shouldPlantBehaviorHandle, this.plantEvents);
		for (IGameBehavior behavior : this.behaviors) {
			behavior.register(game, plantEvents);
		}
	}

	private boolean placePlant(ServerPlayerEntity player, Plot plot, BlockPos pos, PlantType plantType) {
		if (!this.plantType.equals(plantType)) {
			return false;
		}

		PlantCoverage coverage = place.place(game.getWorld(), plot, pos);
		if (coverage == null) return false;

		Plant plant = plot.plants.addPlant(plantType, coverage);
		game.invoker(MpEvents.ADD_PLANT).onAddPlant(player, plot, plant);

		return true;
	}

	private boolean removePlant(ServerPlayerEntity player, Plot plot, Plant plant) {
		if (!this.plantType.equals(plant.type())) {
			return false;
		}

		ServerWorld world = game.getWorld();
		for (BlockPos plantPos : plant.coverage()) {
			world.setBlockState(plantPos, Blocks.AIR.getDefaultState(), Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.UPDATE_NEIGHBORS);
		}

		plot.plants.removePlant(plant);

		return true;
	}

	private ActionResultType onBreakBlock(ServerPlayerEntity player, BlockPos pos, BlockState state) {
		Plot plot = plots.getPlotFor(player);
		if (plot == null) {
			return ActionResultType.PASS;
		}

		Plant plant = plot.plants.getPlantAt(pos, this.plantType);
		if (plant != null) {
			return this.onBreakPlant(player, pos, plot, plant);
		} else {
			return ActionResultType.PASS;
		}
	}

	private ActionResultType onBreakPlant(ServerPlayerEntity player, BlockPos pos, Plot plot, Plant plant) {
		ServerWorld world = game.getWorld();

		BlockState block = world.getBlockState(pos);
		game.invoker(MpEvents.BREAK_PLANT).breakPlant(player, plot, plant);

		if (drops != null) {
			for (ItemStack item : drops.collectDrops(player, game, pos, block)) {
				Block.spawnAsEntity(world, pos, item);
			}
		}

		return ActionResultType.FAIL;
	}

	private void onTickPlot(ServerPlayerEntity player, Plot plot) {
		List<Plant> plants = plot.plants.getPlantsByType(this.plantType);
		if (!plants.isEmpty()) {
			this.plantEvents.invoker(MpEvents.TICK_PLANTS).onTickPlants(player, plot, plants);
		}
	}

	private static final class Drops {
		private static final Codec<Drops> PLANT_CODEC = PlantItemType.CODEC.xmap(type -> new Drops(type, Optional.empty()), drops -> drops.plant);

		private static final Codec<Drops> RECORD_CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					PlantItemType.CODEC.fieldOf("plant").forGetter(c -> c.plant),
					ResourceLocation.CODEC.optionalFieldOf("loot_table").forGetter(c -> Optional.ofNullable(c.lootTable))
			).apply(instance, Drops::new);
		});

		public static final Codec<Drops> CODEC = Codec.either(RECORD_CODEC, PLANT_CODEC)
				.xmap(
						either -> either.map(Function.identity(), Function.identity()),
						drops -> drops.lootTable != null ? Either.left(drops) : Either.right(drops)
				);

		private final PlantItemType plant;
		private final ResourceLocation lootTable;

		public Drops(PlantItemType plant, @Nullable ResourceLocation lootTable) {
			this.plant = plant;
			this.lootTable = lootTable;
		}

		private Drops(PlantItemType plant, Optional<ResourceLocation> lootTable) {
			this(plant, lootTable.orElse(null));
		}

		public Collection<ItemStack> collectDrops(ServerPlayerEntity player, IGamePhase game, BlockPos pos, BlockState block) {
			List<ItemStack> drops = new ArrayList<>();

			ItemStack plantItem = game.invoker(MpEvents.CREATE_PLANT_ITEM).createPlantItem(plant);
			drops.add(plantItem);

			LootTable lootTable = this.getLootTable(game.getServer());
			if (lootTable != null) {
				LootContext context = new LootContext.Builder(game.getWorld())
						.withParameter(LootParameters.THIS_ENTITY, player)
						.withParameter(LootParameters.ORIGIN, Vector3d.copyCentered(pos))
						.withParameter(LootParameters.BLOCK_STATE, block)
						.withRandom(player.getRNG())
						.withLuck(player.getLuck())
						.build(LootParameterSets.BLOCK);

				drops.addAll(lootTable.generate(context));
			}

			return drops;
		}

		@Nullable
		private LootTable getLootTable(MinecraftServer server) {
			if (this.lootTable != null) {
				return server.getLootTableManager().getLootTableFromLocation(this.lootTable);
			} else {
				return null;
			}
		}
	}
}
