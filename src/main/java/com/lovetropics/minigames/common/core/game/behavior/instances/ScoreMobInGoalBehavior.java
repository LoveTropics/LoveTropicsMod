package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.util.EntityTemplate;
import com.lovetropics.minigames.common.util.LinearSpline;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public record ScoreMobInGoalBehavior(
		String spawnRegion,
		EntityTemplate scoringEntity,
		LinearSpline initialCountByPlayers,
		Map<GameTeamKey, String> teamGoalRegions,
		StatisticKey<Integer> statistic
) implements IGameBehavior {
	public static final MapCodec<ScoreMobInGoalBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("spawn_region").forGetter(ScoreMobInGoalBehavior::spawnRegion),
			EntityTemplate.CODEC.fieldOf("scoring_entity").forGetter(ScoreMobInGoalBehavior::scoringEntity),
			LinearSpline.CODEC.optionalFieldOf("initial_count_by_players", LinearSpline.constant(1.0f)).forGetter(ScoreMobInGoalBehavior::initialCountByPlayers),
			Codec.unboundedMap(GameTeamKey.CODEC, Codec.STRING).fieldOf("team_goal_regions").forGetter(ScoreMobInGoalBehavior::teamGoalRegions),
			StatisticKey.typedCodec(Integer.class).fieldOf("statistic").forGetter(ScoreMobInGoalBehavior::statistic)
	).apply(i, ScoreMobInGoalBehavior::new));

	private static final Component POSITIVE_EMOTE = Component.literal("⭐").withStyle(ChatFormatting.GOLD);
	private static final Component NEGATIVE_EMOTE = Component.literal("☠").withStyle(ChatFormatting.RED);

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return GameBehaviorTypes.SCORE_MOB_IN_GOAL;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		BlockBox spawnBox = game.mapRegions().getOrThrow(spawnRegion);

		TeamState teams = game.instanceState().getOrThrow(TeamState.KEY);
		if (teams.getTeamKeys().size() != 2) {
			throw new GameException(Component.literal("Only 2 teams are supported"));
		}

		List<Goal> goals = new ArrayList<>();
		for (Map.Entry<GameTeamKey, String> entry : teamGoalRegions.entrySet()) {
			BlockBox box = game.mapRegions().getOrThrow(entry.getValue());
			GameTeamKey defensiveTeam = entry.getKey();
			GameTeamKey offensiveTeam = teams.getTeamKeys().stream().filter(k -> !k.equals(defensiveTeam)).findFirst().orElseThrow();
			goals.add(new Goal(box, defensiveTeam, offensiveTeam));
		}

		events.listen(GamePhaseEvents.START, () -> {
			int initialCount = Mth.floor(initialCountByPlayers.get(game.participants().size()));
			for (int i = 0; i < initialCount; i++) {
				spawnScoringEntity(game, spawnBox);
			}
		});

		events.listen(GameLivingEntityEvents.TICK, entity -> {
			if (entity.getType() != scoringEntity.type()) {
				return;
			}
			Goal goal = getGoalAt(goals, entity.position());
			if (goal != null) {
				scoreGoal(game, teams, goal, entity, spawnBox);
				entity.discard();
			}
		});
	}

	private void spawnScoringEntity(IGamePhase game, BlockBox spawnBox) {
		BlockPos spawnPos = findSuitableSpawn(game, spawnBox);
		scoringEntity.spawn(game.level(), spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
	}

	private BlockPos findSuitableSpawn(IGamePhase game, BlockBox box) {
		BlockPos size = box.size();
		int halfSizeX = Mth.positiveCeilDiv(size.getX(), 2);
		int halfSizeY = Mth.positiveCeilDiv(size.getY(), 2);
		int halfSizeZ = Mth.positiveCeilDiv(size.getZ(), 2);
		BlockPos bestPos = box.centerBlock();
		int bestEntityCount = Integer.MAX_VALUE;
		for (BlockPos pos : BlockPos.withinManhattan(box.centerBlock(), halfSizeX, halfSizeY, halfSizeZ)) {
			if (!box.contains(pos)) {
				continue;
			}
			int entityCount = game.level().getEntities(scoringEntity.type(), new AABB(pos), e -> true).size();
			if (entityCount == 0) {
				return pos;
			} else if (entityCount < bestEntityCount) {
				bestPos = pos.immutable();
				bestEntityCount = entityCount;
			}
		}
		return bestPos;
	}

	private void scoreGoal(IGamePhase game, TeamState teams, Goal goal, LivingEntity entity, BlockBox spawnBox) {
		game.statistics().forTeam(goal.offensiveTeam).incrementInt(statistic, 1);

		ServerPlayer scoringPlayer = entity.getLastAttacker() instanceof ServerPlayer p ? p : null;
		// Not necessarily the player who scored
		GameTeam scoringTeam = Objects.requireNonNull(teams.getTeamByKey(goal.offensiveTeam));

		spawnScoringEntity(game, spawnBox);

		addScoreEffects(game, teams, goal, scoringPlayer, scoringTeam);
	}

	private static void addScoreEffects(IGamePhase game, TeamState teams, Goal goal, @Nullable ServerPlayer scoringPlayer, GameTeam scoringTeam) {
		Component scoringName = scoringPlayer != null ? scoringPlayer.getDisplayName() : MinigameTexts.UNKNOWN;
		boolean opposingGoal = scoringPlayer == null || teams.isOnTeam(scoringPlayer, goal.defensiveTeam);

		Component title = opposingGoal ? NEGATIVE_EMOTE : POSITIVE_EMOTE;
		Component subtitle = MinigameTexts.POINT_SCORED.apply(scoringName, scoringTeam.config().styledName());

		int fade = SharedConstants.TICKS_PER_SECOND / 4;
		int length = SharedConstants.TICKS_PER_SECOND * 2;
		game.allPlayers().showTitle(title, subtitle, fade, length, fade);
		game.allPlayers().playSound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
	}

	@Nullable
	private Goal getGoalAt(List<Goal> goals, Vec3 position) {
		for (Goal goal : goals) {
			if (goal.box.contains(position)) {
				return goal;
			}
		}
		return null;
	}

	private record Goal(BlockBox box, GameTeamKey defensiveTeam, GameTeamKey offensiveTeam) {
	}
}
