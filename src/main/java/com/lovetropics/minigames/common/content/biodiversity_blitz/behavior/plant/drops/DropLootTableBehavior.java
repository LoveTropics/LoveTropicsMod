package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.drops;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantType;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public final class DropLootTableBehavior implements IGameBehavior {
	public static final Codec<DropLootTableBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			PlantType.CODEC.fieldOf("id").forGetter(c -> c.plantType),
			ResourceLocation.CODEC.fieldOf("loot_table").forGetter(c -> c.lootTable)
	).apply(i, DropLootTableBehavior::new));
	private final PlantType plantType;
	private final ResourceLocation lootTable;

	public DropLootTableBehavior(PlantType plantType, ResourceLocation lootTable) {
		this.plantType = plantType;
		this.lootTable = lootTable;
	}

	private IGamePhase game = null;

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;
		events.listen(BbPlantEvents.BREAK, this::dropLoot);

		events.listen(GamePlayerEvents.USE_BLOCK, (player, world, blockPos, hand, blockRayTraceResult) -> {
			BlockPos pos = blockRayTraceResult.getBlockPos();

			Plot plot = game.getState().getOrThrow(PlotsState.KEY).getPlotFor(player);
			if (plot == null || !plot.bounds.contains(pos)) {
				return InteractionResult.PASS;
			}

			BlockState state = world.getBlockState(pos);

			if (state.hasProperty(BlockStateProperties.AGE_7) && state.getValue(BlockStateProperties.AGE_7) == 7) {
				Plant plant = plot.plants.getPlantAt(pos);

				if (plant != null && plant.type().equals(this.plantType)) {

					dropLoot(player, plot, plant, pos);
					world.setBlock(pos, state.setValue(BlockStateProperties.AGE_7, 0), 3);
					return InteractionResult.SUCCESS;
				}
			}

			return InteractionResult.PASS;
		});
	}

	private void dropLoot(ServerPlayer player, Plot plot, Plant plant, BlockPos pos) {
		LootTable lootTable = this.getLootTable(game.getServer());
		if (lootTable != null) {
			ServerLevel world = game.getWorld();

			LootParams params = this.buildLootParams(player, pos);
			for (ItemStack stack : lootTable.getRandomItems(params)) {
				Block.popResource(world, pos, stack);
			}
		}
	}

	private LootParams buildLootParams(ServerPlayer player, BlockPos pos) {
		return new LootParams.Builder(player.serverLevel())
				.withParameter(LootContextParams.THIS_ENTITY, player)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
				.withParameter(LootContextParams.BLOCK_STATE, player.level().getBlockState(pos))
				.withParameter(LootContextParams.TOOL, player.getUseItem())
				.withLuck(player.getLuck())
				.create(LootContextParamSets.BLOCK);
	}

	@Nullable
	private LootTable getLootTable(MinecraftServer server) {
		if (this.lootTable != null) {
			return server.getLootData().getLootTable(this.lootTable);
		} else {
			return null;
		}
	}
}
