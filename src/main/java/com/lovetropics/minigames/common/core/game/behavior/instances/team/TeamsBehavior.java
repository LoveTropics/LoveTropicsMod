package com.lovetropics.minigames.common.core.game.behavior.instances.team;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigList;
import com.lovetropics.minigames.common.core.game.behavior.config.BehaviorConfig;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
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
				CFG_FRIENDLY_FIRE.orElse(false).forGetter(c -> c.friendlyFire)
		).apply(instance, TeamsBehavior::new);
	});

	private final Map<TeamKey, ScorePlayerTeam> scoreboardTeams = new Object2ObjectOpenHashMap<>();

	private final boolean friendlyFire;

	private TeamState teams;

	public TeamsBehavior(boolean friendlyFire) {
		this.friendlyFire = friendlyFire;
	}

	@Override
	public ConfigList getConfigurables() {
		return ConfigList.builder()
				.with(CFG_FRIENDLY_FIRE, friendlyFire)
				.build();
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		teams = game.getState().getOrThrow(TeamState.KEY);

		events.listen(GamePhaseEvents.START, () -> onStart(game));
		events.listen(GamePhaseEvents.DESTROY, () -> onDestroy(game));
		events.listen(GamePlayerEvents.ALLOCATE_ROLES, allocator -> reassignPlayerRoles(game, allocator));

		events.listen(GamePlayerEvents.SET_ROLE, this::onPlayerSetRole);
		events.listen(GamePlayerEvents.LEAVE, this::removePlayerFromTeams);
		events.listen(GamePlayerEvents.DAMAGE, this::onPlayerHurt);
		events.listen(GamePlayerEvents.ATTACK, this::onPlayerAttack);

		MinecraftServer server = game.getServer();
		ServerScoreboard scoreboard = server.getScoreboard();

		for (TeamKey teamKey : teams) {
			// generate a unique team id since we want to have concurrent games!
			String teamId = teamKey.key + "_" + RandomStringUtils.randomAlphabetic(3);

			ScorePlayerTeam scoreboardTeam = scoreboard.createTeam(teamId);
			scoreboardTeam.setDisplayName(new StringTextComponent(teamKey.name));
			scoreboardTeam.setColor(teamKey.text);
			scoreboardTeam.setAllowFriendlyFire(friendlyFire);

			scoreboardTeams.put(teamKey, scoreboardTeam);
		}

		game.getStatistics().global().set(StatisticKey.TEAMS, true);
	}

	private void reassignPlayerRoles(IGamePhase game, TeamAllocator<PlayerRole, ServerPlayerEntity> allocator) {
		// force all assigned players to be a participant
		for (UUID uuid : teams.getAllocations().getAssignedPlayers()) {
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

	private void onStart(IGamePhase game) {
		teams.getAllocations().allocate(game.getParticipants(), (player, team) -> {
			addPlayerToTeam(game, player, team);
		});
	}

	private void onPlayerSetRole(ServerPlayerEntity player, @Nullable PlayerRole role, @Nullable PlayerRole lastRole) {
		if (lastRole == PlayerRole.PARTICIPANT && role != PlayerRole.PARTICIPANT) {
			removePlayerFromTeams(player);
		}
	}

	private void addPlayerToTeam(IGamePhase game, ServerPlayerEntity player, TeamKey team) {
		teams.addPlayerTo(player, team);

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
