package com.lovetropics.minigames.common.core.game.behavior.instances.world;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public final class GenerateEntitiesBehavior extends ChunkGeneratingBehavior {
	public static final Codec<GenerateEntitiesBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Registry.ENTITY_TYPE.fieldOf("entity").forGetter(c -> c.type),
				Codec.INT.optionalFieldOf("min_per_chunk", 0).forGetter(c -> c.minPerChunk),
				Codec.INT.optionalFieldOf("max_per_chunk", 1).forGetter(c -> c.maxPerChunk)
		).apply(instance, GenerateEntitiesBehavior::new);
	});

	private final EntityType<?> type;
	private final int minPerChunk;
	private final int maxPerChunk;

	public GenerateEntitiesBehavior(EntityType<?> type, int minPerChunk, int maxPerChunk) {
		this.type = type;
		this.minPerChunk = minPerChunk;
		this.maxPerChunk = maxPerChunk;
	}

	@Override
	protected void generateChunk(IGamePhase game, ServerWorld world, Chunk chunk) {
		ChunkPos chunkPos = chunk.getPos();
		int minX = chunkPos.getMinBlockX();
		int minZ = chunkPos.getMinBlockZ();

		Random random = world.random;

		int count = random.nextInt(maxPerChunk - minPerChunk + 1) + minPerChunk;
		for (int i = 0; i < count; i++) {
			int x = minX + random.nextInt(16);
			int z = minZ + random.nextInt(16);

			BlockPos pos = world.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING, new BlockPos(x, 0, z));
			type.spawn(world, null, null, pos, SpawnReason.CHUNK_GENERATION, false, false);
		}
	}
}
