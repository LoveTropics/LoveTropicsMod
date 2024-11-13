package com.lovetropics.minigames.common.content.block_party;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.SpawnBuilder;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class BlockPartyBehavior implements IGameBehavior {
	public static final MapCodec<BlockPartyBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("floor").forGetter(c -> c.floorRegionKey),
			MoreCodecs.arrayOrUnit(BuiltInRegistries.BLOCK.byNameCodec(), Block[]::new).fieldOf("blocks").forGetter(c -> c.blocks),
			Codec.INT.optionalFieldOf("quad_size", 3).forGetter(c -> c.quadSize),
			Codec.LONG.optionalFieldOf("max_time", 20L * 5).forGetter(c -> c.maxTime),
			Codec.LONG.optionalFieldOf("min_time", 20L * 2).forGetter(c -> c.minTime),
			Codec.INT.optionalFieldOf("time_decay_rounds", 5).forGetter(c -> c.timeDecayRounds),
			Codec.LONG.optionalFieldOf("interval", 20L * 3).forGetter(c -> c.interval),
			Codec.INT.optionalFieldOf("knockback_after_round", Integer.MAX_VALUE).forGetter(c -> c.knockbackAfterAround),
			Codec.BOOL.optionalFieldOf("teams_support", false).forGetter(c -> c.teamsSupport)
	).apply(i, BlockPartyBehavior::new));

	private final String floorRegionKey;
	private final Block[] blocks;
	private final int quadSize;

	private final long maxTime;
	private final long minTime;
	private final int timeDecayRounds;
	private final long interval;
	private final int knockbackAfterAround;
	private final boolean teamsSupport;

	private IGamePhase game;
	private BlockBox floorRegion;

	private int quadCountX;
	private int quadCountZ;

	@Nullable
	private State state;

	public BlockPartyBehavior(String floorRegionKey, Block[] blocks, int quadSize, long maxTime, long minTime, int timeDecayRounds, long interval, int knockbackAfterAround, boolean teamsSupport) {
		this.floorRegionKey = floorRegionKey;
		this.blocks = blocks;
		this.quadSize = quadSize;
		this.maxTime = maxTime;
		this.minTime = minTime;
		this.timeDecayRounds = timeDecayRounds;
		this.interval = interval;
		this.knockbackAfterAround = knockbackAfterAround;
		this.teamsSupport = teamsSupport;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;

		if (blocks.length == 0) {
			throw new GameException(Component.literal("No blocks defined!"));
		}

		floorRegion = game.mapRegions().getOrThrow(floorRegionKey);
		BlockPos floorSize = floorRegion.size();
		quadCountX = floorSize.getX() / quadSize;
		quadCountZ = floorSize.getZ() / quadSize;

		events.listen(GamePhaseEvents.START, () -> {
			state = startCountingDown(0);
		});

		events.listen(GamePlayerEvents.SPAWN, (playerId, spawn, role) -> spawnPlayer(spawn));

		events.listen(GamePhaseEvents.TICK, this::tick);

		events.listen(GamePlayerEvents.DAMAGE_AMOUNT, (player, damageSource, amount, originalAmount) -> hasKnockback(state) ? 0.0f : amount);
		events.listen(GamePlayerEvents.ATTACK, (player, target) -> target instanceof Player && !hasKnockback(state) ? InteractionResult.FAIL : InteractionResult.PASS);
		events.listen(GamePlayerEvents.DAMAGE, (player, damageSource, amount) -> hasKnockback(state) ? InteractionResult.PASS : InteractionResult.FAIL);
	}

	private boolean hasKnockback(@Nullable State state) {
		return state != null && state.hasKnockback();
	}

	private void spawnPlayer(SpawnBuilder spawn) {
		BlockPos floorPos = floorRegion.sample(game.level().getRandom());
		spawn.teleportTo(game.level(), floorPos.above());
	}

	private void onStateChange(State oldState, @Nullable State newState) {
		if (hasKnockback(newState) && !hasKnockback(oldState)) {
			PlayerSet allPlayers = game.allPlayers();
			allPlayers.sendMessage(BlockPartyTexts.KNOCKBACK_ENABLED);
			allPlayers.playSound(SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
		}
	}

	private void tick() {
		if (state == null) {
			return;
		}

		State newState = state.tick(game);
		if (state != newState) {
			onStateChange(state, newState);
		}
		state = newState;

		if (newState == null) {
			game.requestStop(GameStopReason.finished());
			return;
		}

		List<ServerPlayer> eliminated = null;

		PlayerSet participants = game.participants();
		for (ServerPlayer player : participants) {
			double y = player.getY();
			if (y < player.level().getMinBuildHeight() || y < floorRegion.min().getY() - 10) {
				if (eliminated == null) {
					eliminated = new ArrayList<>();
				}

				eliminated.add(player);

				game.allPlayers().sendMessage(MinigameTexts.ELIMINATED.apply(player.getDisplayName()));
			}
		}

		if (eliminated != null) {
			for (ServerPlayer player : eliminated) {
				game.setPlayerRole(player, PlayerRole.SPECTATOR);
			}

			if(teamsSupport) {
				TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
				if(teams != null){
					if(teams.getTeamKeys().size() > 1){
						Map<GameTeamKey, List<ServerPlayer>> collect = participants.stream().collect(Collectors.groupingBy(teams::getTeamForPlayer));
						List<GameTeamKey> teamsWithPlayers = collect.keySet().stream().filter(gameTeamKey -> !collect.get(gameTeamKey).isEmpty()).toList();
						if(teamsWithPlayers.size() == 1){
							GameTeamKey winningTeamKey = teamsWithPlayers.getFirst();
							GameTeam winningTeam = teams.getTeamByKey(winningTeamKey);
							Component message;
							if (winningTeam != null) {
								MutableComponent styledTeamName = winningTeam.config().styledName();
								message = MinigameTexts.PLAYER_WON.apply(styledTeamName).withStyle(ChatFormatting.GREEN);
//								game.statistics().global().set(StatisticKey.WINNING_PLAYER, PlayerKey.from(winningPlayer));

								game.invoker(GameLogicEvents.GAME_OVER).onGameWonBy(winningTeam);
							} else {
								message = MinigameTexts.NOBODY_WON.copy().withStyle(ChatFormatting.RED);
							}

							state = new Ending(game.ticks() + 20 * 5);

							game.allPlayers().sendMessage(message);
						} else if(teamsWithPlayers.isEmpty()){
							Component message;
							message = MinigameTexts.NOBODY_WON.copy().withStyle(ChatFormatting.RED);
							state = new Ending(game.ticks() + 20 * 5);
							game.allPlayers().sendMessage(message);
						}
						return;
					}
				}
			}
			if (participants.size() <= 1) {
				ServerPlayer winningPlayer = getWinningPlayer();

				Component message;
				if (winningPlayer != null) {
					message = MinigameTexts.PLAYER_WON.apply(winningPlayer.getDisplayName()).withStyle(ChatFormatting.GREEN);
					game.invoker(GameLogicEvents.GAME_OVER).onGameWonBy(winningPlayer);
				} else {
					message = MinigameTexts.NOBODY_WON.copy().withStyle(ChatFormatting.RED);
				}

				state = new Ending(game.ticks() + 20 * 5);

				game.allPlayers().sendMessage(message);
			}
		}
	}

	@Nullable
	private ServerPlayer getWinningPlayer() {
		PlayerSet participants = game.participants();
		if (participants.isEmpty()) {
			return null;
		} else {
			return participants.iterator().next();
		}
	}

	CountingDown startCountingDown(int round) {
		ServerLevel world = game.level();
		Floor floor = Floor.generate(world.random, quadCountX, quadCountZ, blocks);
		floor.set(world, floorRegion, quadSize);

		Block target = floor.target;

		ItemStack targetStack = new ItemStack(target);
		targetStack.setCount(64);

		Component name = BlockPartyTexts.STAND_ON_BLOCK.apply(targetStack.getDisplayName())
				.withStyle(style -> style.withBold(true).withItalic(false));
		targetStack.set(DataComponents.CUSTOM_NAME, name);

		for (ServerPlayer player : game.participants()) {
			player.getInventory().clearContent();
			for (int i = 0; i < 9; i++) {
				player.getInventory().add(targetStack.copy());
			}
		}

		float lerp = (float) round / timeDecayRounds;
		long duration = Mth.floor(Mth.clampedLerp(maxTime, minTime, lerp));
		return new CountingDown(round + 1, game.ticks() + duration, floor);
	}

	Interval startInterval(int round, Floor floor) {
		floor.removeNonTargets(game.level(), floorRegion);
		return new Interval(round, game.ticks() + interval);
	}

	interface State {
		@Nullable
		State tick(IGamePhase game);

		boolean hasKnockback();
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
				for (ServerPlayer player : game.allPlayers()) {
					long remainingTicks = breakAt - time;
					Component message = BlockPartyTexts.BREAK_IN_SECONDS.apply(remainingTicks / SharedConstants.TICKS_PER_SECOND).withStyle(ChatFormatting.GOLD);
					player.displayClientMessage(message, true);
				}
			}

			if (time > breakAt) {
				return startInterval(round, floor);
			}

			return this;
		}

		@Override
		public boolean hasKnockback() {
			return round >= knockbackAfterAround;
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
			if (time > nextAt) {
				return startCountingDown(round + 1);
			}
			return this;
		}

		@Override
		public boolean hasKnockback() {
			return round >= knockbackAfterAround;
		}
	}

	record Ending(long endAt) implements State {
		@Override
		public State tick(IGamePhase game) {
			if (game.ticks() > endAt) {
				return null;
			}
			return this;
		}

		@Override
		public boolean hasKnockback() {
			return false;
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

		static Floor generate(RandomSource random, int quadCountX, int quadCountZ, Block[] blocks) {
			Block[] quads = new Block[quadCountX * quadCountZ];

			for (int z = 0; z < quadCountZ; z++) {
				for (int x = 0; x < quadCountX; x++) {
					quads[x + z * quadCountX] = blocks[random.nextInt(blocks.length)];
				}
			}

			Block target = Util.getRandom(quads, random);
			return new Floor(quads, quadCountX, quadCountZ, target);
		}

		void set(ServerLevel world, BlockBox box, int quadSize) {
			for (BlockPos pos : box) {
				int localX = pos.getX() - box.min().getX();
				int localZ = pos.getZ() - box.min().getZ();
				int x = Mth.clamp(localX / quadSize, 0, quadCountX - 1);
				int z = Mth.clamp(localZ / quadSize, 0, quadCountZ - 1);

				Block quad = quads[x + z * quadCountX];
				world.setBlockAndUpdate(pos, quad.defaultBlockState());
			}
		}

		void removeNonTargets(ServerLevel world, BlockBox box) {
			for (BlockPos pos : box) {
				BlockState state = world.getBlockState(pos);
				if (!state.is(target)) {
					world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
				}
			}
		}
	}
}
