package com.lovetropics.minigames.common.core.game.behavior.instances.team;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.config.BehaviorConfig;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.RandomStringUtils;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public final class TeamsBehavior implements IGameBehavior {
	private static final BehaviorConfig<Boolean> CFG_FRIENDLY_FIRE = BehaviorConfig.fieldOf("friendly_fire", Codec.BOOL);

	public static final Codec<TeamsBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				CFG_FRIENDLY_FIRE.orElse(false).forGetter(c -> c.friendlyFire),
				Codec.BOOL.optionalFieldOf("static_team_ids", false).forGetter(c -> c.staticTeamIds)
		).apply(instance, TeamsBehavior::new);
	});

	private final Map<GameTeamKey, ScorePlayerTeam> scoreboardTeams = new Object2ObjectOpenHashMap<>();

	private final boolean friendlyFire;
	private final boolean staticTeamIds;

	private TeamState teams;

	public TeamsBehavior(boolean friendlyFire, boolean staticTeamIds) {
		this.friendlyFire = friendlyFire;
		this.staticTeamIds = staticTeamIds;
	}

	@Override
	public ConfigList getConfigurables() {
		return ConfigList.builder()
				.with(CFG_FRIENDLY_FIRE, friendlyFire)
				.build();
	}

	@Override
	public IGameBehavior configure(ConfigList configs) {
		return new TeamsBehavior(CFG_FRIENDLY_FIRE.getValue(configs), staticTeamIds);
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		teams = game.getState().getOrThrow(TeamState.KEY);

		this.addTeamsToScoreboard(game);

		Map<ServerPlayerEntity, GameTeamKey> assignedTeams = new Reference2ObjectOpenHashMap<>();
		teams.getAllocations().allocate(game.getParticipants(), assignedTeams::put);

		events.listen(GamePlayerEvents.ADD, player -> {
			GameTeamKey team = assignedTeams.remove(player);
			addPlayerToTeam(game, player, team);
		});

		events.listen(GamePhaseEvents.DESTROY, () -> onDestroy(game));
		events.listen(GamePlayerEvents.ALLOCATE_ROLES, allocator -> reassignPlayerRoles(game, allocator));

		events.listen(GamePlayerEvents.SET_ROLE, this::onPlayerSetRole);
		events.listen(GamePlayerEvents.LEAVE, this::removePlayerFromTeams);
		events.listen(GamePlayerEvents.DAMAGE, this::onPlayerHurt);
		events.listen(GamePlayerEvents.ATTACK, this::onPlayerAttack);

		game.getStatistics().global().set(StatisticKey.TEAMS, true);
	}

	private void addTeamsToScoreboard(IGamePhase game) {
		MinecraftServer server = game.getServer();
		ServerScoreboard scoreboard = server.getScoreboard();

		for (GameTeam team : teams) {
			String teamId = createTeamId(team.key());

			ScorePlayerTeam scoreboardTeam = scoreboard.createTeam(teamId);
			scoreboardTeam.setDisplayName(team.config().name());
			scoreboardTeam.setColor(team.config().formatting());
			scoreboardTeam.setAllowFriendlyFire(friendlyFire);

			scoreboardTeams.put(team.key(), scoreboardTeam);
		}
	}

	private String createTeamId(GameTeamKey team) {
		if (staticTeamIds) {
			return team.id();
		} else {
			return team.id() + "_" + RandomStringUtils.randomAlphabetic(3);
		}
	}

	private void reassignPlayerRoles(IGamePhase game, TeamAllocator<PlayerRole, ServerPlayerEntity> allocator) {
		// force all assigned players to be a participant
		for (UUID uuid : teams.getAssignedPlayers()) {
			ServerPlayerEntity player = game.getAllPlayers().getPlayerBy(uuid);
			if (player != null) {
				allocator.addPlayer(player, PlayerRole.PARTICIPANT);
			}
		}
	}

	private void onDestroy(IGamePhase game) {
		ServerScoreboard scoreboard = game.getServer().getScoreboard();
		for (ScorePlayerTeam team : scoreboardTeams.values()) {
			scoreboard.removeTeam(team);
		}
	}

	private void onPlayerSetRole(ServerPlayerEntity player, @Nullable PlayerRole role, @Nullable PlayerRole lastRole) {
		if (lastRole == PlayerRole.PARTICIPANT && role != PlayerRole.PARTICIPANT) {
			removePlayerFromTeams(player);
		}
	}

	private void addPlayerToTeam(IGamePhase game, ServerPlayerEntity player, GameTeamKey teamKey) {
		GameTeam team = teams.getTeamByKey(teamKey);
		if (team == null) {
			return;
		}

		teams.addPlayerTo(player, teamKey);

		game.getStatistics().forPlayer(player).set(StatisticKey.TEAM, teamKey);

		ServerScoreboard scoreboard = player.server.getScoreboard();
		ScorePlayerTeam scoreboardTeam = scoreboardTeams.get(teamKey);
		scoreboard.addPlayerToTeam(player.getScoreboardName(), scoreboardTeam);

		ITextComponent teamName = team.config().name().deepCopy().appendString(" Team!")
				.mergeStyle(TextFormatting.BOLD, team.config().formatting());

		player.sendStatusMessage(
				new StringTextComponent("You are on ").mergeStyle(TextFormatting.GRAY).appendSibling(teamName),
				false
		);
	}

	private void removePlayerFromTeams(ServerPlayerEntity player) {
		teams.removePlayer(player);

		ServerScoreboard scoreboard = player.server.getScoreboard();
		scoreboard.removePlayerFromTeams(player.getScoreboardName());
	}

	private ActionResultType onPlayerHurt(ServerPlayerEntity player, DamageSource source, float amount) {
		if (!friendlyFire && teams.areSameTeam(source.getTrueSource(), player)) {
			return ActionResultType.FAIL;
		}
		return ActionResultType.PASS;
	}

	private ActionResultType onPlayerAttack(ServerPlayerEntity player, Entity target) {
		if (!friendlyFire && teams.areSameTeam(player, target)) {
			return ActionResultType.FAIL;
		}
		return ActionResultType.PASS;
	}
}
