package com.lovetropics.minigames.common.content.trash_dive;

import com.lovetropics.minigames.common.content.block.LoveTropicsBlocks;
import com.lovetropics.minigames.common.content.block.TrashBlock;
import com.lovetropics.minigames.common.content.block.TrashBlock.Attachment;
import com.lovetropics.minigames.common.content.block.TrashType;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameWorldEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.resources.IResource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.chunk.IChunk;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Random;

public final class PlaceTrashBehavior implements IGameBehavior {
	public static final Codec<PlaceTrashBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				ResourceLocation.CODEC.fieldOf("positionData").forGetter(c -> c.positionData),
				Codec.INT.optionalFieldOf("centerY", 75).forGetter(c -> c.centerY),
				Codec.INT.optionalFieldOf("range", 50).forGetter(c -> c.range),
				Codec.INT.optionalFieldOf("density", 4).forGetter(c -> c.density)
		).apply(instance, PlaceTrashBehavior::new);
	});

	private final TrashType[] trashTypes = TrashType.values();

	private final ResourceLocation positionData;
	private final int centerY;
	private final int range;
	private final int density;

	public PlaceTrashBehavior(ResourceLocation positionData, int centerY, int range, int density) {
		this.positionData = positionData;
		this.centerY = centerY;
		this.range = range;
		this.density = density;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		Long2ObjectMap<LongList> trashByChunk = loadTrashByChunk(game);

		events.listen(GameWorldEvents.CHUNK_LOAD, (chunk) -> {
			LongList positions = trashByChunk.remove(chunk.getPos().toLong());
			if (positions == null) {
				return;
			}

			Random random = chunk.getWorldForge().getRandom();
			BlockPos.Mutable pos = new BlockPos.Mutable();

			LongListIterator iterator = positions.iterator();
			while (iterator.hasNext()) {
				pos.set(iterator.nextLong());

				int count = random.nextInt(density);
				for (int i = 0; i < count; i++) {
					// range defines 3 standard deviations from the mean (centerY)
					pos.setY(centerY + (int) ((random.nextGaussian() / 3) * range));
					tryPlaceTrash(chunk, pos, random);
				}
			}
		});
	}

	private Long2ObjectMap<LongList> loadTrashByChunk(IGamePhase game) {
		LongBuffer candidatePositions;
		try (IResource res = game.getServer().getDataPackRegistries().getResourceManager().getResource(positionData)) {
			InputStream in = res.getInputStream();
			final byte[] data = new byte[8];
			final ByteBuffer buf = ByteBuffer.allocate(in.available());
			candidatePositions = buf.asLongBuffer();
			while (in.read(data) > 0) {
				buf.put(data);
			}
			candidatePositions.position(0);
		} catch (Exception e) {
			throw new GameException(new StringTextComponent("Unexpected error reading trash position data"), e);
		}

		Long2ObjectMap<LongList> trashByChunk = new Long2ObjectOpenHashMap<>();
		while (candidatePositions.hasRemaining()) {
			long candidatePos = candidatePositions.get();

			int chunkX = BlockPos.getX(candidatePos) >> 4;
			int chunkZ = BlockPos.getZ(candidatePos) >> 4;
			long chunkKey = ChunkPos.asLong(chunkX, chunkZ);

			trashByChunk.computeIfAbsent(chunkKey, l -> new LongArrayList()).add(candidatePos);
		}

		return trashByChunk;
	}

	private void tryPlaceTrash(IChunk chunk, BlockPos pos, Random random) {
		if (chunk.getBlockState(pos).getBlock() == Blocks.WATER) {
			TrashType trashType = trashTypes[random.nextInt(trashTypes.length)];
			chunk.setBlockState(pos, LoveTropicsBlocks.TRASH.get(trashType).getDefaultState()
							.setValue(TrashBlock.WATERLOGGED, true)
							.setValue(TrashBlock.ATTACHMENT, Block.canSupportRigidBlock(chunk, pos.below()) ? Attachment.FLOOR : Attachment.random(random))
							.setValue(TrashBlock.FACING, Direction.from2DDataValue(random.nextInt(4))),
					false
			);
		}
	}
}
