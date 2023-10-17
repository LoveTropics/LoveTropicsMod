package com.lovetropics.minigames.common.core.game.behavior.instances.world;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;

public final class GenerateEntitiesBehavior extends ChunkGeneratingBehavior {
	public static final MapCodec<GenerateEntitiesBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("entity").forGetter(c -> c.type),
			Codec.INT.optionalFieldOf("min_per_chunk", 0).forGetter(c -> c.minPerChunk),
			Codec.INT.optionalFieldOf("max_per_chunk", 1).forGetter(c -> c.maxPerChunk)
	).apply(i, GenerateEntitiesBehavior::new));

	private final EntityType<?> type;
	private final int minPerChunk;
	private final int maxPerChunk;

	public GenerateEntitiesBehavior(EntityType<?> type, int minPerChunk, int maxPerChunk) {
		this.type = type;
		this.minPerChunk = minPerChunk;
		this.maxPerChunk = maxPerChunk;
	}

	@Override
	protected void generateChunk(IGamePhase game, ServerLevel world, LevelChunk chunk) {
		ChunkPos chunkPos = chunk.getPos();
		int minX = chunkPos.getMinBlockX();
		int minZ = chunkPos.getMinBlockZ();

		RandomSource random = world.random;

		int count = random.nextInt(maxPerChunk - minPerChunk + 1) + minPerChunk;
		for (int i = 0; i < count; i++) {
			int x = minX + random.nextInt(16);
			int z = minZ + random.nextInt(16);

			BlockPos pos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z));
			type.spawn(world, null, (Player) null, pos, MobSpawnType.CHUNK_GENERATION, false, false);
		}
	}
}
