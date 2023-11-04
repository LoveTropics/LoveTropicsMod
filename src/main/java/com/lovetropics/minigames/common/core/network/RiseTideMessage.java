package com.lovetropics.minigames.common.core.network;

import com.lovetropics.minigames.common.content.survive_the_tide.TideFiller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RiseTideMessage(BlockPos min, BlockPos max) {
	public void encode(final FriendlyByteBuf output) {
		output.writeBlockPos(min);
		output.writeBlockPos(max);
	}

	public RiseTideMessage(final FriendlyByteBuf input) {
		this(input.readBlockPos(), input.readBlockPos());
	}

	public static void handle(final RiseTideMessage message, final Supplier<NetworkEvent.Context> ctx) {
		final ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			return;
		}
		final BlockPos min = message.min;
		final BlockPos max = message.max;
		final int minChunkX = SectionPos.blockToSectionCoord(min.getX());
		final int minChunkZ = SectionPos.blockToSectionCoord(min.getZ());
		final int maxChunkX = SectionPos.blockToSectionCoord(max.getX());
		final int maxChunkZ = SectionPos.blockToSectionCoord(max.getZ());
		for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
			for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
				final LevelChunk chunk = level.getChunk(chunkX, chunkZ);
				TideFiller.fillChunk(min.getX(), min.getZ(), max.getX(), max.getZ(), chunk, min.getY(), max.getY());
			}
		}
	}
}
