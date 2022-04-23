package com.lovetropics.minigames.common.core.game.behavior.instances.world;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FillChestsByMarkerBehavior extends ChunkGeneratingBehavior {
	public static final Codec<FillChestsByMarkerBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.unboundedMap(ForgeRegistries.BLOCKS.getCodec(), ResourceLocation.CODEC).fieldOf("loot_tables").forGetter(c -> c.lootTableByMarker)
		).apply(instance, FillChestsByMarkerBehavior::new);
	});

	private final Map<Block, ResourceLocation> lootTableByMarker;

	public FillChestsByMarkerBehavior(Map<Block, ResourceLocation> lootTableByMarker) {
		this.lootTableByMarker = lootTableByMarker;
	}

	@Override
	protected void generateChunk(IGamePhase game, ServerLevel world, LevelChunk chunk) {
		List<BlockPos> chestPositions = new ArrayList<>();
		for (BlockPos pos : chunk.getBlockEntitiesPos()) {
			BlockEntity entity = chunk.getBlockEntity(pos);
			if (entity instanceof ChestBlockEntity) {
				chestPositions.add(pos);
			}
		}

		for (BlockPos pos : chestPositions) {
			BlockPos belowPos = pos.below();

			BlockState belowState = world.getBlockState(belowPos);
			ResourceLocation lootTable = getLootTableFor(belowState.getBlock());
			if (lootTable != null) {
				Direction facing = belowState.getValue(BlockStateProperties.HORIZONTAL_FACING);
				setChest(world, belowPos, lootTable, facing);

				world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			}
		}
	}

	private void setChest(ServerLevel world, BlockPos pos, ResourceLocation lootTable, Direction facing) {
		world.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
		BlockEntity chest = world.getBlockEntity(pos);
		if (chest instanceof ChestBlockEntity) {
			((ChestBlockEntity) chest).setLootTable(lootTable, world.random.nextLong());
		}
	}

	private ResourceLocation getLootTableFor(Block block) {
		return this.lootTableByMarker.get(block);
	}
}
