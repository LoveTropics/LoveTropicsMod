package com.lovetropics.minigames.common.core.game.behavior.instances.world;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Collection;
import java.util.List;

public class FillChestsByMarkerBehavior extends ChunkGeneratingBehavior {
	public static final MapCodec<FillChestsByMarkerBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			BuiltInRegistries.BLOCK.byNameCodec().fieldOf("marker").forGetter(c -> c.marker),
			SimpleWeightedRandomList.wrappedCodec(ResourceKey.codec(Registries.LOOT_TABLE)).fieldOf("loot_tables").forGetter(c -> c.lootTables),
			Codec.FLOAT.optionalFieldOf("percentage", 1.0f).forGetter(c -> c.percentage),
			Codec.INT.optionalFieldOf("max_per_chunk", Integer.MAX_VALUE).forGetter(c -> c.maxPerChunk),
			Codec.INT.optionalFieldOf("max_per_section", Integer.MAX_VALUE).forGetter(c -> c.maxPerSection)
	).apply(i, FillChestsByMarkerBehavior::new));

	private final Block marker;
	private final SimpleWeightedRandomList<ResourceKey<LootTable>> lootTables;
	private final float percentage;
	private final int maxPerChunk;
	private final int maxPerSection;

	public FillChestsByMarkerBehavior(Block marker, SimpleWeightedRandomList<ResourceKey<LootTable>> lootTables, float percentage, int maxPerChunk, int maxPerSection) {
		this.marker = marker;
		this.lootTables = lootTables;
		this.percentage = percentage;
		this.maxPerChunk = maxPerChunk;
		this.maxPerSection = maxPerSection;
	}

	@Override
	protected void generateChunk(IGamePhase game, ServerLevel world, LevelChunk chunk) {
		ObjectArrayList<BlockPos> chestPositions = collectChestPositions(chunk);
		if (chestPositions.isEmpty()) {
			return;
		}

		RandomSource random = world.random;
		Util.shuffle(chestPositions, random);

		if (percentage < 1.0f) {
			int index = Mth.ceil(chestPositions.size() * percentage);
			trimChests(world, chestPositions, index);
		}

		trimChests(world, chestPositions, maxPerChunk);

		for (ObjectList<BlockPos> sectionChests : partitionBySection(chestPositions)) {
			trimChests(world, sectionChests, maxPerSection);

			for (BlockPos pos : sectionChests) {
				BlockPos belowPos = pos.below();
				BlockState belowState = world.getBlockState(belowPos);
				Direction facing = belowState.getValue(BlockStateProperties.HORIZONTAL_FACING);
				world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
				lootTables.getRandomValue(random).ifPresent(lootTable -> {
					setChest(world, belowPos, lootTable, facing);
				});
			}
		}
	}

	private static Collection<ObjectList<BlockPos>> partitionBySection(ObjectList<BlockPos> chestPositions) {
		if (chestPositions.isEmpty()) {
			return List.of();
		}
		Int2ObjectMap<ObjectList<BlockPos>> partitioned = new Int2ObjectArrayMap<>();
		for (BlockPos pos : chestPositions) {
			int sectionY = SectionPos.blockToSectionCoord(pos.getY());
			partitioned.computeIfAbsent(sectionY, i -> new ObjectArrayList<>()).add(pos);
		}
		return partitioned.values();
	}

	private static void trimChests(ServerLevel level, ObjectList<BlockPos> positions, int count) {
		for (int i = positions.size() - 1; i >= count; i--) {
			BlockPos pos = positions.remove(i);
			level.removeBlock(pos, false);
			level.removeBlock(pos.below(), false);
		}
	}

	private ObjectArrayList<BlockPos> collectChestPositions(LevelChunk chunk) {
		ObjectArrayList<BlockPos> chestPositions = new ObjectArrayList<>();
		for (BlockPos pos : chunk.getBlockEntitiesPos()) {
			if (chunk.getBlockEntity(pos) instanceof ChestBlockEntity) {
				if (chunk.getBlockState(pos.below()).is(marker)) {
					chestPositions.add(pos);
				}
			}
		}
		return chestPositions;
	}

	private void setChest(ServerLevel world, BlockPos pos, ResourceKey<LootTable> lootTable, Direction facing) {
		world.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
		if (world.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
			chest.setLootTable(lootTable, world.random.nextLong());
		}
		world.getChunkSource().getLightEngine().checkBlock(pos);
	}
}
