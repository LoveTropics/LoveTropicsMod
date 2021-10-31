package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GrowCoconutsBehavior implements IGameBehavior {

	public static final Codec<GrowCoconutsBehavior> CODEC = Codec.INT.fieldOf("interval")
			.xmap(GrowCoconutsBehavior::new, b -> b.interval)
			.codec();

	private static final RegistryObject<Block> COCONUT = RegistryObject.of(new ResourceLocation("tropicraft", "coconut"), ForgeRegistries.BLOCKS);
	private final int interval;
	private final WeakHashMap<Plant, List<Pair<BlockPos, Direction>>> candidatePositions = new WeakHashMap<>();
	
	private IGamePhase game;
	private PlotsState plots;

	public GrowCoconutsBehavior(int interval) {
		this.interval = interval;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		this.game = game;
		this.plots = game.getState().getOrThrow(PlotsState.KEY);
		events.listen(BbPlantEvents.TICK, this::tick);
		events.listen(GamePlayerEvents.BREAK_BLOCK, this::breakFromCoconut);
		events.listen(BbPlantEvents.BREAK, this::onPlantBreak);
	}

	private void tick(ServerPlayerEntity player, Plot plot, List<Plant> plants) {
		if (game.ticks() % interval == 0) {
			for (Plant plant : plants) {
				List<Pair<BlockPos, Direction>> candidates = candidatePositions.computeIfAbsent(plant, p -> p.functionalCoverage().stream()
					.filter(bp -> player.world.getBlockState(bp).isIn(BlockTags.LOGS))
					.filter(bp -> IntStream.range(0, 4)
							.mapToObj(Direction::byHorizontalIndex)
							.allMatch(d -> player.world.getBlockState(bp.offset(d).up()).isIn(BlockTags.LEAVES)))
					.flatMap(bp -> {
						List<Pair<BlockPos, Direction>> ret = new ArrayList<>();
						for (int i = 0; i < 4; i++) {
							Direction dir = Direction.byHorizontalIndex(i);
							BlockPos pos = bp.offset(dir);
							if (player.world.isAirBlock(pos) || player.world.getBlockState(pos).getBlock() == COCONUT.get()) {
								ret.add(Pair.of(pos, dir));
							}
						}
						return ret.stream();
					}).collect(Collectors.toList()));
				
				Collections.shuffle(candidates);
				for (Pair<BlockPos, Direction> candidate : candidates) {
					if (player.world.isAirBlock(candidate.getLeft())) {
						player.world.setBlockState(candidate.getLeft(), COCONUT.get().getDefaultState().with(DirectionalBlock.FACING, candidate.getRight().getOpposite()));
						break;
					}
				}
			}
		}
	}

	// TODO: manual handling of coverage is not a good solution! next year: change our approach to how we're handling dynamic coverages like this
	private ActionResultType breakFromCoconut(ServerPlayerEntity player, BlockPos pos, BlockState state, Hand hand) {
		if (state.getBlock() == COCONUT.get()) {
			BlockPos trunkPos = pos.offset(state.get(DirectionalBlock.FACING));
			game.invoker(GamePlayerEvents.BREAK_BLOCK).onBreakBlock(player, trunkPos, player.world.getBlockState(trunkPos), Hand.MAIN_HAND);
			return ActionResultType.FAIL;
		}
		return ActionResultType.PASS;
	}

	private void onPlantBreak(ServerPlayerEntity player, Plot plot, Plant plant, BlockPos pos) {
		List<Pair<BlockPos, Direction>> candidates = candidatePositions.get(plant);
		if (candidates != null) {
			candidates.forEach(p -> player.world.destroyBlock(p.getLeft(), false));
		}
	}
}
