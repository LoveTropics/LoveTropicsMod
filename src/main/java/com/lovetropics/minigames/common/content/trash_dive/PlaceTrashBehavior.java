package com.lovetropics.minigames.common.content.trash_dive;

import com.lovetropics.minigames.common.content.block.LoveTropicsBlocks;
import com.lovetropics.minigames.common.content.block.TrashBlock;
import com.lovetropics.minigames.common.content.block.TrashBlock.Attachment;
import com.lovetropics.minigames.common.content.block.TrashType;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.resources.IResource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

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
	public void register(IGameInstance game, GameEventListeners events) throws GameException {
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

		ServerWorld world = game.getWorld();
		Random random = world.rand;

		BlockPos.Mutable pos = new BlockPos.Mutable();
		while (candidatePositions.hasRemaining()) {
			pos.setPos(candidatePositions.get());
			pos.setY(centerY);
			int count = random.nextInt(density);
			for (int i = 0; i < count; i++) {
				// range defines 3 standard deviations from the mean (centerY)
				pos.move(Direction.UP, (int) ((random.nextGaussian() / 3) * range));
				tryPlaceTrash(world, pos);
				pos.setY(centerY);
			}
		}
	}

	private void tryPlaceTrash(ServerWorld world, BlockPos pos) {
		Random random = world.rand;
		if (world.getBlockState(pos).getBlock() == Blocks.WATER) {
			TrashType trashType = trashTypes[random.nextInt(trashTypes.length)];
			world.setBlockState(pos, LoveTropicsBlocks.TRASH.get(trashType).getDefaultState()
					.with(TrashBlock.WATERLOGGED, true)
					.with(TrashBlock.ATTACHMENT, Block.hasSolidSideOnTop(world, pos.down()) ? Attachment.FLOOR : Attachment.random(random))
					.with(TrashBlock.FACING, Direction.byHorizontalIndex(random.nextInt(4)))
			);
		}
	}
}
