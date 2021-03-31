package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.ControlCommand;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.*;
import com.lovetropics.minigames.common.core.game.polling.PollingGameInstance;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.instances.TeamKey;
import com.lovetropics.minigames.common.core.game.state.instances.TeamState;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import com.lovetropics.minigames.common.util.MoreCodecs;
import com.lovetropics.minigames.common.util.Scheduler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;
import java.util.*;

public final class TeamsBehavior implements IGameBehavior {
	public static final Codec<TeamsBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				TeamKey.CODEC.listOf().fieldOf("teams").forGetter(c -> c.teams),
				Codec.BOOL.optionalFieldOf("friendly_fire", false).forGetter(c -> c.friendlyFire),
				Codec.unboundedMap(Codec.STRING, MoreCodecs.UUID_STRING.listOf()).fieldOf("assign").orElseGet(Object2ObjectOpenHashMap::new).forGetter(c -> c.assignedTeams)
		).apply(instance, TeamsBehavior::new);
	});

	private final List<TeamKey> teams;
	private final Map<TeamKey, ScorePlayerTeam> scoreboardTeams = new Object2ObjectOpenHashMap<>();
	private final Map<String, List<UUID>> assignedTeams;

	private final List<TeamKey> pollingTeams;

	private final boolean friendlyFire;

	private final Map<UUID, TeamKey> teamPreferences = new Object2ObjectOpenHashMap<>();

	private TeamState teamState;

	public TeamsBehavior(List<TeamKey> teams, boolean friendlyFire, Map<String, List<UUID>> assignedTeams) {
		this.teams = teams;
		this.friendlyFire = friendlyFire;
		this.assignedTeams = assignedTeams;

		this.pollingTeams = new ArrayList<>(teams.size());
		for (TeamKey team : teams) {
			if (!assignedTeams.containsKey(team.key)) {
				this.pollingTeams.add(team);
			}
		}
	}

	@Override
	public void registerPolling(PollingGameInstance registerGame, EventRegistrar events) throws GameException {
		events.listen(GamePollingEvents.START, this::onStartPolling);
		events.listen(GamePollingEvents.PLAYER_REGISTER, this::onPlayerRegister);
	}

	@Override
	public void registerState(GameStateMap state) {
		teamState = state.register(TeamState.TYPE, new TeamState(this.teams));
	}

	@Override
	public void register(IGameInstance game, EventRegistrar events) {
		events.listen(GameLifecycleEvents.ASSIGN_ROLES, this::assignPlayerRoles);
		events.listen(GameLifecycleEvents.START, this::onStart);
		events.listen(GameLifecycleEvents.FINISH, this::onFinish);

		events.listen(GamePlayerEvents.CHANGE_ROLE, this::onPlayerChangeRole);
		events.listen(GamePlayerEvents.LEAVE, this::onPlayerLeave);
		events.listen(GamePlayerEvents.DAMAGE, this::onPlayerHurt);
		events.listen(GamePlayerEvents.ATTACK, this::onPlayerAttack);

		MinecraftServer server = game.getServer();
		ServerScoreboard scoreboard = server.getScoreboard();

		for (TeamKey teamKey : teams) {
			ScorePlayerTeam team = scoreboard.getTeam(teamKey.key);
			if (team != null) {
				scoreboard.removeTeam(team);
			}

			ScorePlayerTeam scoreboardTeam = scoreboard.createTeam(teamKey.key);
			scoreboardTeam.setDisplayName(new StringTextComponent(teamKey.name));
			scoreboardTeam.setColor(teamKey.text);
			scoreboardTeam.setAllowFriendlyFire(friendlyFire);

			scoreboardTeams.put(teamKey, scoreboardTeam);
		}

		game.getStatistics().getGlobal().set(StatisticKey.TEAMS, true);
	}

	private void onStartPolling(PollingGameInstance game) {
		for (TeamKey team : pollingTeams) {
			game.addControlCommand("join_team_" + team.key, ControlCommand.forEveryone(source -> {
				ServerPlayerEntity player = source.asPlayer();
				if (game.isPlayerRegistered(player)) {
					onRequestJoinTeam(player, team);
				} else {
					player.sendStatusMessage(new StringTextComponent("You have not yet joined this minigame!").mergeStyle(TextFormatting.RED), false);
				}
			}));
		}
	}

	private void onRequestJoinTeam(ServerPlayerEntity player, TeamKey team) {
		teamPreferences.put(player.getUniqueID(), team);

		player.sendStatusMessage(
				new StringTextComponent("You have requested to join ").mergeStyle(TextFormatting.GRAY)
						.appendSibling(new StringTextComponent(team.name).mergeStyle(team.text, TextFormatting.BOLD)),
				false
		);
	}

	private void onPlayerRegister(PollingGameInstance game, ServerPlayerEntity player, @Nullable PlayerRole role) {
		if (role != PlayerRole.SPECTATOR && pollingTeams.size() > 1) {
			Scheduler.INSTANCE.submit(server -> {
				sendTeamSelectionTo(player);
			}, 1);
		}
	}

	private void sendTeamSelectionTo(ServerPlayerEntity player) {
		player.sendStatusMessage(new StringTextComponent("This is a team-based game!").mergeStyle(TextFormatting.GOLD, TextFormatting.BOLD), false);
		player.sendStatusMessage(new StringTextComponent("You can select a team preference by clicking the links below:").mergeStyle(TextFormatting.GRAY), false);

		for (TeamKey team : pollingTeams) {
			Style linkStyle = Style.EMPTY
					.setFormatting(team.text)
					.setUnderlined(true)
					.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minigame join_team_" + team.key))
					.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Join " + team.name)));

			player.sendStatusMessage(
					new StringTextComponent(" - ").mergeStyle(TextFormatting.GRAY)
							.appendSibling(new StringTextComponent("Join " + team.name).setStyle(linkStyle)),
					false
			);
		}
	}

	private void assignPlayerRoles(IGameInstance game, List<ServerPlayerEntity> participants, List<ServerPlayerEntity> spectators) {
		Set<UUID> requiredPlayers = new ObjectOpenHashSet<>();
		for (List<UUID> assignedTeam : assignedTeams.values()) {
			requiredPlayers.addAll(assignedTeam);
		}

		Random random = new Random();

		for (UUID id : requiredPlayers) {
			// try find this player within the spectators list
			ServerPlayerEntity requiredPlayer = null;
			for (ServerPlayerEntity player : spectators) {
				if (player.getUniqueID().equals(id)) {
					requiredPlayer = player;
					break;
				}
			}

			// if this required player is in the spectators list, try to swap them with another player
			if (requiredPlayer != null) {
				ServerPlayerEntity swapWithPlayer = null;

				// find another player to swap with
				List<ServerPlayerEntity> shuffledParticipants = new ArrayList<>(participants);
				Collections.shuffle(shuffledParticipants, random);

				for (ServerPlayerEntity swapCandidate : shuffledParticipants) {
					if (!requiredPlayers.contains(swapCandidate.getUniqueID())) {
						swapWithPlayer = swapCandidate;
						break;
					}
				}

				// if we found a player to swap with, do that
				if (swapWithPlayer != null) {
					participants.remove(swapWithPlayer);
					spectators.add(swapWithPlayer);
					spectators.remove(requiredPlayer);
					participants.add(requiredPlayer);
				}
			}
		}
	}

	private void onFinish(IGameInstance game) {
		ServerScoreboard scoreboard = game.getServer().getScoreboard();
		for (ScorePlayerTeam team : scoreboardTeams.values()) {
			scoreboard.removeTeam(team);
		}
	}

	private void onStart(IGameInstance game) {
		Set<UUID> assignedPlayers = new ObjectOpenHashSet<>();

		for (Map.Entry<String, List<UUID>> entry : assignedTeams.entrySet()) {
			TeamKey team = teamState.getTeamByKey(entry.getKey());
			List<UUID> players = entry.getValue();

			for (UUID id : players) {
				ServerPlayerEntity player = game.getParticipants().getPlayerBy(id);
				if (player != null) {
					addPlayerToTeam(game, player, team);
					assignedPlayers.add(id);
				}
			}
		}

		if (!pollingTeams.isEmpty()) {
			TeamAllocator teamAllocator = new TeamAllocator(pollingTeams);
			for (ServerPlayerEntity player : game.getParticipants()) {
				if (!assignedPlayers.contains(player.getUniqueID())) {
					TeamKey teamPreference = teamPreferences.get(player.getUniqueID());
					teamAllocator.addPlayer(player, teamPreference);
				}
			}

			teamAllocator.allocate((player, team) -> {
				addPlayerToTeam(game, player, team);
			});
		}
	}

	private void onPlayerChangeRole(IGameInstance game, ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
		if (role == PlayerRole.SPECTATOR) {
			removePlayerFromTeams(player);
		}
	}

	private void addPlayerToTeam(IGameInstance game, ServerPlayerEntity player, TeamKey team) {
		teamState.addPlayerTo(player, team);

		game.getStatistics().forPlayer(player).set(StatisticKey.TEAM, team);

		ServerScoreboard scoreboard = player.server.getScoreboard();
		ScorePlayerTeam scoreboardTeam = scoreboardTeams.get(team);
		scoreboard.addPlayerToTeam(player.getScoreboardName(), scoreboardTeam);

		player.sendStatusMessage(
				new StringTextComponent("You are on ").mergeStyle(TextFormatting.GRAY)
						.appendSibling(new StringTextComponent(team.name + " Team!").mergeStyle(TextFormatting.BOLD, team.text)),
				false
		);
	}

	private void removePlayerFromTeams(ServerPlayerEntity player) {
		teamState.removePlayer(player);

		ServerScoreboard scoreboard = player.server.getScoreboard();
		scoreboard.removePlayerFromTeams(player.getScoreboardName());
	}

	private void onPlayerLeave(IGameInstance game, ServerPlayerEntity player) {
		removePlayerFromTeams(player);
	}

	private ActionResultType onPlayerHurt(final IGameInstance game, ServerPlayerEntity player, DamageSource source, float amount) {
		if (!friendlyFire && teamState.areSameTeam(source.getTrueSource(), player)) {
			return ActionResultType.FAIL;
		}
		return ActionResultType.PASS;
	}

	private ActionResultType onPlayerAttack(IGameInstance game, ServerPlayerEntity player, Entity target) {
		if (!friendlyFire && teamState.areSameTeam(player, target)) {
			return ActionResultType.FAIL;
		}
		return ActionResultType.PASS;
	}
}
