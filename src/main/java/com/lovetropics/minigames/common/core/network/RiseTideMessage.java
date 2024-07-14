package com.lovetropics.minigames.common.core.network;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.survive_the_tide.TideFiller;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RiseTideMessage(BlockPos min, BlockPos max) implements CustomPacketPayload {
    public static final Type<RiseTideMessage> TYPE = new Type<>(LoveTropics.location("rise_tide"));

	public static final StreamCodec<ByteBuf, RiseTideMessage> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, RiseTideMessage::min,
			BlockPos.STREAM_CODEC, RiseTideMessage::max,
			RiseTideMessage::new
	);

	public static void handle(final RiseTideMessage message, final IPayloadContext context) {
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

    @Override
    public Type<RiseTideMessage> type() {
        return TYPE;
    }
}
