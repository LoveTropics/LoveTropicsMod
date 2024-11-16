package com.lovetropics.minigames.common.core.game.behavior.instances.team;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.config.BehaviorConfig;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameTeamEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Map;

public final class TeamsBehavior implements IGameBehavior {
	private static final ResourceLocation CONFIG_ID = LoveTropics.location("teams");
	private static final BehaviorConfig<Boolean> CFG_FRIENDLY_FIRE = BehaviorConfig.fieldOf("friendly_fire", Codec.BOOL);

	public static final MapCodec<TeamsBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			CFG_FRIENDLY_FIRE.orElse(false).forGetter(c -> c.friendlyFire),
			Codec.BOOL.optionalFieldOf("static_team_ids", false).forGetter(c -> c.staticTeamIds)
	).apply(i, TeamsBehavior::new));

	private final Map<GameTeamKey, PlayerTeam> scoreboardTeams = new Object2ObjectOpenHashMap<>();

	private final boolean friendlyFire;
	private final boolean staticTeamIds;

	private TeamState teams;

	public TeamsBehavior(boolean friendlyFire, boolean staticTeamIds) {
		this.friendlyFire = friendlyFire;
		this.staticTeamIds = staticTeamIds;
	}

	@Override
	public ConfigList getConfigurables() {
		return ConfigList.builder(CONFIG_ID)
				.with(CFG_FRIENDLY_FIRE, friendlyFire)
				.build();
	}

	@Override
	public IGameBehavior configure(Map<ResourceLocation, ConfigList> configs) {
		return new TeamsBehavior(CFG_FRIENDLY_FIRE.getValue(configs.get(CONFIG_ID)), staticTeamIds);
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		teams = game.instanceState().getOrThrow(TeamState.KEY);

		addTeamsToScoreboard(game);

		events.listen(GamePhaseEvents.CREATE, () -> {
			teams.allocatePlayers(game.participants());
			game.invoker(GameTeamEvents.TEAMS_ALLOCATED).onTeamsAllocated();
		});

		events.listen(GamePlayerEvents.ADD, player -> {
			GameTeamKey teamKey = teams.getTeamForPlayer(player);
			if (teamKey != null) {
				GameTeam team = teams.getTeamByKey(teamKey);
				if (team != null) {
					applyTeamToPlayer(game, team, player);
				}
			}
		});

		events.listen(GamePhaseEvents.DESTROY, () -> onDestroy(game));
		events.listen(GamePlayerEvents.ALLOCATE_ROLES, allocator -> reassignPlayerRoles(game, allocator));

		events.listen(GamePlayerEvents.LEAVE, player -> removePlayerFromTeams(game, player));
		events.listen(GamePlayerEvents.DAMAGE, this::onPlayerHurt);
		events.listen(GamePlayerEvents.ATTACK, this::onPlayerAttack);

		game.statistics().global().set(StatisticKey.TEAMS, true);
	}

	private void addTeamsToScoreboard(IGamePhase game) {
		MinecraftServer server = game.server();
		ServerScoreboard scoreboard = server.getScoreboard();

		for (GameTeam team : teams) {
			String teamId = createTeamId(team.key());
			PlayerTeam scoreboardTeam = getOrCreateScoreboardTeam(scoreboard, team, teamId);
			scoreboardTeams.put(team.key(), scoreboardTeam);
		}
	}

	private PlayerTeam getOrCreateScoreboardTeam(ServerScoreboard scoreboard, GameTeam team, String teamId) {
		PlayerTeam scoreboardTeam = scoreboard.getPlayerTeam(teamId);
		if (scoreboardTeam == null) {
			scoreboardTeam = scoreboard.addPlayerTeam(teamId);
			scoreboardTeam.setDisplayName(team.config().name());
			scoreboardTeam.setColor(team.config().formatting());
			scoreboardTeam.setAllowFriendlyFire(friendlyFire);
		}

		return scoreboardTeam;
	}

	private String createTeamId(GameTeamKey team) {
		if (staticTeamIds) {
			return team.id();
		} else {
			return team.id() + "_" + RandomStringUtils.randomAlphabetic(3);
		}
	}

	private void reassignPlayerRoles(IGamePhase game, TeamAllocator<PlayerRole, ServerPlayer> allocator) {
		// force all assigned players to be a participant
		teams.getPlayersWithAssignments(game.allPlayers()).forEach(player ->
				allocator.addPlayer(player, PlayerRole.PARTICIPANT)
		);
	}

	private void onDestroy(IGamePhase game) {
		ServerScoreboard scoreboard = game.server().getScoreboard();
		for (PlayerTeam team : scoreboardTeams.values()) {
			scoreboard.removePlayerTeam(team);
		}
	}

	private void applyTeamToPlayer(IGamePhase game, GameTeam team, ServerPlayer player) {
		game.invoker(GameTeamEvents.SET_GAME_TEAM).onSetGameTeam(player, teams, team.key());

		game.statistics().forPlayer(player).set(StatisticKey.TEAM, team.key());

		ServerScoreboard scoreboard = player.server.getScoreboard();
		PlayerTeam scoreboardTeam = scoreboardTeams.get(team.key());
		scoreboard.addPlayerToTeam(player.getScoreboardName(), scoreboardTeam);

		Component teamName = team.config().name().copy()
				.withStyle(ChatFormatting.BOLD, team.config().formatting());

		player.displayClientMessage(MinigameTexts.ON_TEAM.apply(teamName), false);
	}

	private void removePlayerFromTeams(IGamePhase game, ServerPlayer player) {
		final var teamKey = teams.removePlayer(player);
		if (teamKey != null) {
			game.invoker(GameTeamEvents.REMOVE_FROM_TEAM).onRemoveFromTeam(player, teams, teamKey);
		}

		ServerScoreboard scoreboard = player.server.getScoreboard();
		scoreboard.removePlayerFromTeam(player.getScoreboardName());
	}

	private InteractionResult onPlayerHurt(ServerPlayer player, DamageSource source, float amount) {
		if (!friendlyFire && teams.areSameTeam(source.getEntity(), player)) {
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}

	private InteractionResult onPlayerAttack(ServerPlayer player, Entity target) {
		if (!friendlyFire && teams.areSameTeam(player, target)) {
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}
}
