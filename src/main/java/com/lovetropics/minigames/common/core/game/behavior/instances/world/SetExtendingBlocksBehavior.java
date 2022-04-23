package com.lovetropics.minigames.common.core.game.behavior.instances.world;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.gen.blockstateprovider.BlockStateProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class SetExtendingBlocksBehavior implements IGameBehavior {
	public static final Codec<SetExtendingBlocksBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.BLOCK_PREDICATE.optionalFieldOf("replace").forGetter(c -> Optional.ofNullable(c.replace)),
				MoreCodecs.BLOCK_STATE_PROVIDER.fieldOf("set").forGetter(c -> c.set),
				MoreCodecs.stringVariants(Direction.values(), Direction::getName).fieldOf("direction").forGetter(c -> c.direction),
				MoreCodecs.arrayOrUnit(Codec.STRING, String[]::new).optionalFieldOf("region", new String[0]).forGetter(c -> c.regionKeys),
				Codec.LONG.fieldOf("start_time").forGetter(c -> c.startTime),
				Codec.LONG.fieldOf("end_time").forGetter(c -> c.endTime),
				Codec.BOOL.optionalFieldOf("notify_neighbors", true).forGetter(c -> c.notifyNeighbors)
		).apply(instance, SetExtendingBlocksBehavior::new);
	});

	private final @Nullable BlockPredicate replace;
	private final BlockStateProvider set;
	private final Direction direction;

	private final String[] regionKeys;
	private final long startTime;
	private final long endTime;

	private final boolean notifyNeighbors;

	private SetExtendingBlocksBehavior(Optional<BlockPredicate> replace, BlockStateProvider set, Direction direction, String[] regionKeys, long startTime, long endTime, boolean notifyNeighbors) {
		this(replace.orElse(null), set, direction, regionKeys, startTime, endTime, notifyNeighbors);
	}

	public SetExtendingBlocksBehavior(BlockPredicate replace, BlockStateProvider set, Direction direction, String[] regionKeys, long startTime, long endTime, boolean notifyNeighbors) {
		this.replace = replace;
		this.set = set;
		this.direction = direction;
		this.regionKeys = regionKeys;
		this.startTime = startTime;
		this.endTime = endTime;
		this.notifyNeighbors = notifyNeighbors;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		List<BlockBox> regions = new ArrayList<>();
		for (String regionKey : regionKeys) {
			regions.addAll(game.getMapRegions().get(regionKey));
		}

		if (regions.isEmpty()) {
			throw new GameException(new StringTextComponent("Regions not specified for extending block set behavior!"));
		}

		events.listen(GamePhaseEvents.TICK, () -> {
			long gameTime = game.ticks();
			if (gameTime >= startTime && gameTime < endTime) {
				long time = gameTime - startTime;
				long timeLength = endTime - startTime + 1;

				float progress = MathHelper.clamp((float) time / timeLength, 0.0F, 1.0F);

				for (BlockBox region : regions) {
					this.tickExtendingInBox(game, region, progress);
				}
			}
		});
	}

	private void tickExtendingInBox(IGamePhase game, BlockBox box, float progress) {
		// TODO: we should be not rewriting blocks that we already placed
		BlockBox extendingBox = this.getExtendingBox(box, progress);

		ServerWorld world = game.getWorld();
		BlockPredicate replace = this.replace;
		BlockStateProvider set = this.set;
		Random random = world.random;

		int flags = Constants.BlockFlags.DEFAULT;
		if (!notifyNeighbors) {
			// the constant name is inverted
			flags |= Constants.BlockFlags.UPDATE_NEIGHBORS;
		}

		for (BlockPos pos : extendingBox) {
			if (replace == null || replace.matches(world, pos)) {
				BlockState state = set.getState(random, pos);
				state = Block.updateFromNeighbourShapes(state, world, pos);
				world.setBlock(pos, state, flags);
			}
		}
	}

	private BlockBox getExtendingBox(BlockBox box, float progress) {
		BlockPos size = box.getSize();

		int totalLength = direction.getAxis().choose(size.getX(), size.getY(), size.getZ());
		int currentLength = MathHelper.floor(totalLength * progress);

		if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
			return box.withMax(box.min.relative(direction, currentLength));
		} else {
			return box.withMin(box.max.relative(direction, currentLength));
		}
	}
}
