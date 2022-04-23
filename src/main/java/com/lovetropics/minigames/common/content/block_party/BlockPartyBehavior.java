package com.lovetropics.minigames.common.content.block_party;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class BlockPartyBehavior implements IGameBehavior {
	public static final Codec<BlockPartyBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.fieldOf("floor").forGetter(c -> c.floorRegionKey),
				MoreCodecs.arrayOrUnit(Registry.BLOCK, Block[]::new).fieldOf("blocks").forGetter(c -> c.blocks),
				Codec.INT.optionalFieldOf("quad_size", 3).forGetter(c -> c.quadSize),
				Codec.LONG.optionalFieldOf("max_time", 20L * 5).forGetter(c -> c.maxTime),
				Codec.LONG.optionalFieldOf("min_time", 20L * 2).forGetter(c -> c.minTime),
				Codec.INT.optionalFieldOf("time_decay_rounds", 5).forGetter(c -> c.timeDecayRounds),
				Codec.LONG.optionalFieldOf("interval", 20L * 3).forGetter(c -> c.interval)
		).apply(instance, BlockPartyBehavior::new);
	});

	private final String floorRegionKey;
	private final Block[] blocks;
	private final int quadSize;

	private final long maxTime;
	private final long minTime;
	private final int timeDecayRounds;
	private final long interval;

	private IGamePhase game;
	private BlockBox floorRegion;

	private int quadCountX;
	private int quadCountZ;

	private State state;

	public BlockPartyBehavior(String floorRegionKey, Block[] blocks, int quadSize, long maxTime, long minTime, int timeDecayRounds, long interval) {
		this.floorRegionKey = floorRegionKey;
		this.blocks = blocks;
		this.quadSize = quadSize;
		this.maxTime = maxTime;
		this.minTime = minTime;
		this.timeDecayRounds = timeDecayRounds;
		this.interval = interval;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;

		if (blocks.length == 0) {
			throw new GameException(new StringTextComponent("No blocks defined!"));
		}

		floorRegion = game.getMapRegions().getOrThrow(floorRegionKey);
		BlockPos floorSize = floorRegion.getSize();
		quadCountX = floorSize.getX() / quadSize;
		quadCountZ = floorSize.getZ() / quadSize;

		events.listen(GamePhaseEvents.START, () -> {
			state = startCountingDown(0);
		});

		events.listen(GamePlayerEvents.SPAWN, (player, role) -> this.spawnPlayer(player));

		events.listen(GamePhaseEvents.TICK, this::tick);
	}

	private void spawnPlayer(ServerPlayerEntity player) {
		BlockPos floorPos = floorRegion.sample(player.getRandom());
		DimensionUtils.teleportPlayerNoPortal(player, game.getDimension(), floorPos.above());
	}

	private void tick() {
		if (state == null) {
			return;
		}

		State newState = state.tick(game);
		state = newState;

		if (newState == null) {
			game.requestStop(GameStopReason.finished());
			return;
		}

		List<ServerPlayerEntity> eliminated = null;

		PlayerSet participants = game.getParticipants();
		for (ServerPlayerEntity player : participants) {
			double y = player.getY();
			if (y < 0 || y < floorRegion.min.getY() - 10) {
				if (eliminated == null) {
					eliminated = new ArrayList<>();
				}

				eliminated.add(player);

				ITextComponent message = new StringTextComponent("\u2620 ")
						.append(player.getDisplayName()).append(" was eliminated!")
						.withStyle(TextFormatting.GRAY);
				game.getAllPlayers().sendMessage(message);
			}
		}

		if (eliminated != null) {
			for (ServerPlayerEntity player : eliminated) {
				game.setPlayerRole(player, PlayerRole.SPECTATOR);
			}

			if (participants.size() <= 1) {
				ServerPlayerEntity winningPlayer = getWinningPlayer();

				ITextComponent message;
				if (winningPlayer != null) {
					message = new StringTextComponent("\u2B50 ")
							.append(winningPlayer.getDisplayName()).append(" won the game!")
							.withStyle(TextFormatting.GREEN);
				} else {
					message = new StringTextComponent("\u2B50 Nobody won the game!")
							.withStyle(TextFormatting.RED);
				}

				state = new Ending(game.ticks() + 20 * 5);

				game.getAllPlayers().sendMessage(message);
			}
		}
	}

	@Nullable
	private ServerPlayerEntity getWinningPlayer() {
		PlayerSet participants = game.getParticipants();
		if (participants.isEmpty()) {
			return null;
		} else {
			return participants.iterator().next();
		}
	}

	CountingDown startCountingDown(int round) {
		ServerWorld world = game.getWorld();
		Floor floor = Floor.generate(world.random, quadCountX, quadCountZ, blocks);
		floor.set(world, floorRegion, quadSize);

		Block target = floor.target;

		ItemStack targetStack = new ItemStack(target);
		targetStack.setCount(64);

		ITextComponent name = new StringTextComponent("Stand on ")
				.append(targetStack.getDisplayName())
				.withStyle(style -> style.withBold(true).withItalic(false));
		targetStack.setHoverName(name);

		for (ServerPlayerEntity player : game.getParticipants()) {
			player.inventory.clearContent();
			for (int i = 0; i < 9; i++) {
				player.inventory.add(targetStack.copy());
			}
		}

		float lerp = (float) round / timeDecayRounds;
		long duration = MathHelper.floor(MathHelper.lerp(lerp, maxTime, minTime));
		return new CountingDown(round + 1, game.ticks() + duration, floor);
	}

	Interval startInterval(int round, Floor floor) {
		floor.removeNonTargets(game.getWorld(), floorRegion);
		return new Interval(round, game.ticks() + interval);
	}

	interface State {
		@Nullable
		State tick(IGamePhase game);
	}

	final class CountingDown implements State {
		private final int round;
		private final long breakAt;

		private final Floor floor;

		CountingDown(int round, long breakAt, Floor floor) {
			this.round = round;
			this.breakAt = breakAt;
			this.floor = floor;
		}

		@Override
		public State tick(IGamePhase game) {
			long time = game.ticks();

			if (time % 10 == 0) {
				for (ServerPlayerEntity player : game.getAllPlayers()) {
					long remainingTicks = breakAt - time;
					ITextComponent message = new StringTextComponent("Break in " + (remainingTicks / 20) + " seconds").withStyle(TextFormatting.GOLD);
					player.displayClientMessage(message, true);
				}
			}

			if (time > this.breakAt) {
				return startInterval(round, floor);
			}

			return this;
		}
	}

	final class Interval implements State {
		private final int round;
		private final long nextAt;

		Interval(int round, long nextAt) {
			this.round = round;
			this.nextAt = nextAt;
		}

		@Override
		public State tick(IGamePhase game) {
			long time = game.ticks();
			if (time > this.nextAt) {
				return startCountingDown(round + 1);
			}
			return this;
		}
	}

	final class Ending implements State {
		private final long endAt;

		Ending(long endAt) {
			this.endAt = endAt;
		}

		@Override
		public State tick(IGamePhase game) {
			if (game.ticks() > endAt) {
				return null;
			}
			return this;
		}
	}

	static final class Floor {
		private final Block[] quads;
		private final int quadCountX;
		private final int quadCountZ;

		private final Block target;

		Floor(Block[] quads, int quadCountX, int quadCountZ, Block target) {
			this.quads = quads;
			this.quadCountX = quadCountX;
			this.quadCountZ = quadCountZ;
			this.target = target;
		}

		static Floor generate(Random random, int quadCountX, int quadCountZ, Block[] blocks) {
			Block[] quads = new Block[quadCountX * quadCountZ];

			for (int z = 0; z < quadCountZ; z++) {
				for (int x = 0; x < quadCountX; x++) {
					quads[x + z * quadCountX] = blocks[random.nextInt(blocks.length)];
				}
			}

			Block target = quads[random.nextInt(quads.length)];

			return new Floor(quads, quadCountX, quadCountZ, target);
		}

		void set(ServerWorld world, BlockBox box, int quadSize) {
			for (BlockPos pos : box) {
				int localX = pos.getX() - box.min.getX();
				int localZ = pos.getZ() - box.min.getZ();
				int x = MathHelper.clamp(localX / quadSize, 0, quadCountX - 1);
				int z = MathHelper.clamp(localZ / quadSize, 0, quadCountZ - 1);

				Block quad = quads[x + z * quadCountX];
				world.setBlockAndUpdate(pos, quad.defaultBlockState());
			}
		}

		void removeNonTargets(ServerWorld world, BlockBox box) {
			for (BlockPos pos : box) {
				BlockState state = world.getBlockState(pos);
				if (!state.getBlockState().is(target)) {
					world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
				}
			}
		}
	}
}
