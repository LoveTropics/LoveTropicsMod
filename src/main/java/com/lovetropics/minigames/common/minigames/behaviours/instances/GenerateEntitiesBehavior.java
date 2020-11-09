package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public final class GenerateEntitiesBehavior extends ChunkGeneratingBehavior {
	private final EntityType<?> type;
	private final int minPerChunk;
	private final int maxPerChunk;

	public GenerateEntitiesBehavior(EntityType<?> type, int minPerChunk, int maxPerChunk) {
		this.type = type;
		this.minPerChunk = minPerChunk;
		this.maxPerChunk = maxPerChunk;
	}

	public static <T> GenerateEntitiesBehavior parse(Dynamic<T> root) {
		EntityType<?> entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(root.get("entity").asString("")));
		int minPerChunk = root.get("min_per_chunk").asInt(0);
		int maxPerChunk = root.get("max_per_chunk").asInt(1);
		return new GenerateEntitiesBehavior(entity, minPerChunk, maxPerChunk);
	}

	@Override
	protected void generateChunk(IMinigameInstance minigame, ServerWorld world, Chunk chunk) {
		ChunkPos chunkPos = chunk.getPos();
		int minX = chunkPos.getXStart();
		int minZ = chunkPos.getZStart();

		Random random = world.rand;

		int count = random.nextInt(maxPerChunk - minPerChunk + 1) + minPerChunk;
		for (int i = 0; i < count; i++) {
			int x = minX + random.nextInt(16);
			int z = minZ + random.nextInt(16);

			BlockPos pos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(x, 0, z));
			type.spawn(world, null, null, pos, SpawnReason.CHUNK_GENERATION, false, false);
		}
	}
}
