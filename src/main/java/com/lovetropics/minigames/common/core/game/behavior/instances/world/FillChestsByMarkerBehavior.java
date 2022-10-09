package com.lovetropics.minigames.common.core.game.behavior.instances.world;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Map;

public class FillChestsByMarkerBehavior extends ChunkGeneratingBehavior {
	public static final Codec<FillChestsByMarkerBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.unboundedMap(ForgeRegistries.BLOCKS.getCodec(), ResourceLocation.CODEC).fieldOf("loot_tables").forGetter(c -> c.lootTableByMarker),
			Codec.FLOAT.optionalFieldOf("percentage", 1.0f).forGetter(c -> c.percentage),
			Codec.INT.optionalFieldOf("max_per_chunk", Integer.MAX_VALUE).forGetter(c -> c.maxPerChunk)
	).apply(i, FillChestsByMarkerBehavior::new));

	private final Map<Block, ResourceLocation> lootTableByMarker;
	private final float percentage;
	private final int maxPerChunk;

	public FillChestsByMarkerBehavior(Map<Block, ResourceLocation> lootTableByMarker, float percentage, int maxPerChunk) {
		this.lootTableByMarker = lootTableByMarker;
		this.percentage = percentage;
		this.maxPerChunk = maxPerChunk;
	}

	@Override
	protected void generateChunk(IGamePhase game, ServerLevel world, LevelChunk chunk) {
		ObjectList<BlockPos> chestPositions = collectChestPositions(chunk);
		if (chestPositions.isEmpty()) {
			return;
		}

		ObjectLists.shuffle(chestPositions, world.random);

		if (percentage < 1.0f) {
			int index = Mth.ceil(chestPositions.size() * percentage);
			trimChests(world, chestPositions, index);
		}

		if (chestPositions.size() > maxPerChunk) {
			trimChests(world, chestPositions, maxPerChunk);
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

	private static void trimChests(ServerLevel level, ObjectList<BlockPos> positions, int count) {
		for (int i = positions.size() - 1; i >= count; i--) {
			BlockPos pos = positions.remove(i);
			level.removeBlock(pos, false);
			level.removeBlock(pos.below(), false);
		}
	}

	private static ObjectList<BlockPos> collectChestPositions(LevelChunk chunk) {
		ObjectList<BlockPos> chestPositions = new ObjectArrayList<>();
		for (BlockPos pos : chunk.getBlockEntitiesPos()) {
			if (chunk.getBlockEntity(pos) instanceof ChestBlockEntity) {
				chestPositions.add(pos);
			}
		}
		return chestPositions;
	}

	private void setChest(ServerLevel world, BlockPos pos, ResourceLocation lootTable, Direction facing) {
		world.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
		if (world.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
			chest.setLootTable(lootTable, world.random.nextLong());
		}
	}

	@Nullable
	private ResourceLocation getLootTableFor(Block block) {
		return this.lootTableByMarker.get(block);
	}
}
