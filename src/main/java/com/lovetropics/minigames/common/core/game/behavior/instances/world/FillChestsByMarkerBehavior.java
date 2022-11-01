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

public class FillChestsByMarkerBehavior extends ChunkGeneratingBehavior {
	public static final Codec<FillChestsByMarkerBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			ForgeRegistries.BLOCKS.getCodec().fieldOf("marker").forGetter(c -> c.marker),
			ResourceLocation.CODEC.fieldOf("loot_table").forGetter(c -> c.lootTable),
			Codec.FLOAT.optionalFieldOf("percentage", 1.0f).forGetter(c -> c.percentage),
			Codec.INT.optionalFieldOf("max_per_chunk", Integer.MAX_VALUE).forGetter(c -> c.maxPerChunk)
	).apply(i, FillChestsByMarkerBehavior::new));

	private final Block marker;
	private final ResourceLocation lootTable;
	private final float percentage;
	private final int maxPerChunk;

	public FillChestsByMarkerBehavior(Block marker, ResourceLocation lootTable, float percentage, int maxPerChunk) {
		this.marker = marker;
		this.lootTable = lootTable;
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
			Direction facing = belowState.getValue(BlockStateProperties.HORIZONTAL_FACING);
			setChest(world, belowPos, lootTable, facing);

			world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		}
	}

	private static void trimChests(ServerLevel level, ObjectList<BlockPos> positions, int count) {
		for (int i = positions.size() - 1; i >= count; i--) {
			BlockPos pos = positions.remove(i);
			level.removeBlock(pos, false);
			level.removeBlock(pos.below(), false);
		}
	}

	private ObjectList<BlockPos> collectChestPositions(LevelChunk chunk) {
		ObjectList<BlockPos> chestPositions = new ObjectArrayList<>();
		for (BlockPos pos : chunk.getBlockEntitiesPos()) {
			if (chunk.getBlockEntity(pos) instanceof ChestBlockEntity) {
				if (chunk.getBlockState(pos.below()).is(marker)) {
					chestPositions.add(pos);
				}
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
}
