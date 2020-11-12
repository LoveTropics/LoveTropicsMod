package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.Scheduler;
import com.lovetropics.minigames.common.minigames.*;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IPollingMinigameBehavior;
import com.lovetropics.minigames.common.minigames.polling.PollingMinigameInstance;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import javax.annotation.Nullable;
import java.util.*;

public final class TeamsBehavior implements IMinigameBehavior, IPollingMinigameBehavior {
	private final List<TeamKey> teams;
	private final Map<TeamKey, MutablePlayerSet> teamPlayers = new Object2ObjectOpenHashMap<>();
	private final Map<TeamKey, ScorePlayerTeam> scoreboardTeams = new Object2ObjectOpenHashMap<>();
	private final Map<String, List<UUID>> assignedTeams;

	private final List<TeamKey> pollingTeams;

	private final boolean friendlyFire;

	private final Map<UUID, TeamKey> teamPreferences = new Object2ObjectOpenHashMap<>();

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

	public static <T> TeamsBehavior parse(Dynamic<T> root) {
		List<TeamKey> teams = root.get("teams").asList(TeamKey::parse);
		boolean friendlyFire = root.get("friendly_fire").asBoolean(false);
		Map<String, List<UUID>> assignedTeams = root.get("assign").asMap(
				key -> key.asString(""),
				value -> value.asList(id -> UUID.fromString(id.asString("")))
		);

		return new TeamsBehavior(teams, friendlyFire, assignedTeams);
	}

	@Override
	public void onStartPolling(PollingMinigameInstance minigame) {
		for (TeamKey team : pollingTeams) {
			minigame.addControlCommand("join_team_" + team.key, ControlCommand.forEveryone(source -> {
				ServerPlayerEntity player = source.asPlayer();
				onRequestJoinTeam(player, team);
			}));
		}
	}

	private void onRequestJoinTeam(ServerPlayerEntity player, TeamKey team) {
		teamPreferences.put(player.getUniqueID(), team);

		player.sendMessage(
				new StringTextComponent("You have requested to join ").applyTextStyle(TextFormatting.GRAY)
						.appendSibling(new StringTextComponent(team.name).applyTextStyles(team.text, TextFormatting.BOLD))
		);
	}

	@Override
	public void onPlayerRegister(PollingMinigameInstance minigame, ServerPlayerEntity player, @Nullable PlayerRole role) {
		if (role != PlayerRole.SPECTATOR && pollingTeams.size() > 1) {
			Scheduler.INSTANCE.submit(server -> {
				sendTeamSelectionTo(player);
			}, 1);
		}
	}

	private void sendTeamSelectionTo(ServerPlayerEntity player) {
		player.sendMessage(new StringTextComponent("This is a team-based game!").applyTextStyles(TextFormatting.GOLD, TextFormatting.BOLD));
		player.sendMessage(new StringTextComponent("You can select a team preference by clicking the links below:").applyTextStyle(TextFormatting.GRAY));

		for (TeamKey team : pollingTeams) {
			Style linkStyle = new Style();
			linkStyle.setColor(team.text);
			linkStyle.setUnderlined(true);
			linkStyle.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minigame join_team_" + team.key));
			linkStyle.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Join " + team.name)));

			player.sendMessage(
					new StringTextComponent(" - ").applyTextStyle(TextFormatting.GRAY)
							.appendSibling(new StringTextComponent("Join " + team.name).setStyle(linkStyle))
			);
		}
	}

	@Override
	public void assignPlayerRoles(IMinigameInstance minigame, List<ServerPlayerEntity> participants, List<ServerPlayerEntity> spectators) {
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

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		MinecraftServer server = minigame.getServer();
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

			teamPlayers.put(teamKey, new MutablePlayerSet(server));
			scoreboardTeams.put(teamKey, scoreboardTeam);
		}

		minigame.getStatistics().getGlobal().set(StatisticKey.TEAMS, true);
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		ServerScoreboard scoreboard = minigame.getServer().getScoreboard();
		for (ScorePlayerTeam team : scoreboardTeams.values()) {
			scoreboard.removeTeam(team);
		}
	}

	@Override
	public void onStart(IMinigameInstance minigame) {
		Set<UUID> assignedPlayers = new ObjectOpenHashSet<>();

		for (Map.Entry<String, List<UUID>> entry : assignedTeams.entrySet()) {
			TeamKey team = getTeamByKey(entry.getKey());
			List<UUID> players = entry.getValue();

			for (UUID id : players) {
				ServerPlayerEntity player = minigame.getParticipants().getPlayerById(id);
				if (player != null) {
					addPlayerToTeam(minigame, player, team);
					assignedPlayers.add(id);
				}
			}
		}

		if (!pollingTeams.isEmpty()) {
			TeamAllocator teamAllocator = new TeamAllocator(pollingTeams);
			for (ServerPlayerEntity player : minigame.getParticipants()) {
				if (!assignedPlayers.contains(player.getUniqueID())) {
					TeamKey teamPreference = teamPreferences.get(player.getUniqueID());
					teamAllocator.addPlayer(player, teamPreference);
				}
			}

			teamAllocator.allocate((player, team) -> {
				addPlayerToTeam(minigame, player, team);
			});
		}
	}

	@Override
	public void onPlayerChangeRole(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
		if (role == PlayerRole.SPECTATOR) {
			removePlayerFromTeams(player);
		}
	}

	private void addPlayerToTeam(IMinigameInstance minigame, ServerPlayerEntity player, TeamKey team) {
		teamPlayers.get(team).add(player);

		minigame.getStatistics().forPlayer(player).set(StatisticKey.TEAM, team);

		ServerScoreboard scoreboard = player.server.getScoreboard();
		ScorePlayerTeam scoreboardTeam = scoreboardTeams.get(team);
		scoreboard.addPlayerToTeam(player.getScoreboardName(), scoreboardTeam);
	}

	private void removePlayerFromTeams(ServerPlayerEntity player) {
		for (TeamKey team : teams) {
			teamPlayers.get(team).remove(player);
		}

		ServerScoreboard scoreboard = player.server.getScoreboard();
		scoreboard.removePlayerFromTeams(player.getScoreboardName());
	}

	@Override
	public void onPlayerLeave(IMinigameInstance minigame, ServerPlayerEntity player) {
		removePlayerFromTeams(player);
	}

	@Override
	public void onPlayerHurt(final IMinigameInstance minigame, LivingHurtEvent event) {
		if (!friendlyFire && areSameTeam(event.getSource().getTrueSource(), event.getEntityLiving())) {
			event.setCanceled(true);
		}
	}

	@Override
	public void onPlayerAttackEntity(final IMinigameInstance minigame, AttackEntityEvent event) {
		if (!friendlyFire && areSameTeam(event.getEntityLiving(), event.getTarget())) {
			event.setCanceled(true);
		}
	}

	public boolean areSameTeam(Entity source, Entity target) {
		if (!(source instanceof PlayerEntity) || !(target instanceof PlayerEntity)) {
			return false;
		}
		TeamKey sourceTeam = getTeamForPlayer((PlayerEntity) source);
		TeamKey targetTeam = getTeamForPlayer((PlayerEntity) target);
		return Objects.equals(sourceTeam, targetTeam);
	}

	@Nullable
	public TeamKey getTeamForPlayer(PlayerEntity player) {
		for (TeamKey team : teams) {
			if (teamPlayers.get(team).contains(player)) {
				return team;
			}
		}
		return null;
	}

	public PlayerSet getPlayersForTeam(TeamKey team) {
		PlayerSet players = teamPlayers.get(team);
		return players != null ? players : PlayerSet.EMPTY;
	}

	public List<TeamKey> getTeams() {
		return teams;
	}

	@Nullable
	public TeamKey getTeamByKey(String key) {
		for (TeamKey team : teams) {
			if (team.key.equals(key)) {
				return team;
			}
		}
		return null;
	}

	public static class TeamKey {
		public final String key;
		public final String name;
		public final DyeColor dye;
		public final TextFormatting text;

		public TeamKey(String key, String name, DyeColor dye, TextFormatting text) {
			this.key = key;
			this.name = name;
			this.dye = dye;
			this.text = text;
		}

		public static <T> TeamKey parse(Dynamic<T> root) {
			String key = root.get("key").asString("");
			String name = root.get("name").asString(key);
			DyeColor dye = DyeColor.byTranslationKey(root.get("dye").asString(""), DyeColor.WHITE);
			TextFormatting text = TextFormatting.getValueByName(root.get("text").asString(""));
			if (text == null) {
				text = TextFormatting.WHITE;
			}
			return new TeamKey(key, name, dye, text);
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;

			if (obj instanceof TeamKey) {
				TeamKey team = (TeamKey) obj;
				return key.equals(team.key);
			}

			return false;
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}
	}
}
