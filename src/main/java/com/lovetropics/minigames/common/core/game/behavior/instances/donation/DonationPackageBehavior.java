package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.GamePackageState;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public final class DonationPackageBehavior implements IGameBehavior {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final MapCodec<DonationPackageBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			DonationPackageData.CODEC.forGetter(c -> c.data),
			DonationPackageNotification.CODEC.optionalFieldOf("notification").forGetter(c -> c.notification),
			GameActionList.PLAYER_CODEC.optionalFieldOf("receive_actions", GameActionList.EMPTY).forGetter(c -> c.receiveActions),
			GameActionList.TEAM_CODEC.optionalFieldOf("team_receive_actions", GameActionList.EMPTY_TEAM).forGetter(c -> c.teamReceiveActions)
	).apply(i, DonationPackageBehavior::new));

	private final DonationPackageData data;
	private final Optional<DonationPackageNotification> notification;
	private final GameActionList<ServerPlayer> receiveActions;
	private final GameActionList<GameTeam> teamReceiveActions;

	public DonationPackageBehavior(DonationPackageData data, Optional<DonationPackageNotification> notification, GameActionList<ServerPlayer> receiveActions, GameActionList<GameTeam> teamReceiveActions) {
		this.data = data;
		this.notification = notification;
		this.receiveActions = receiveActions;
		this.teamReceiveActions = teamReceiveActions;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePackageEvents.RECEIVE_PACKAGE, gamePackage -> onGamePackageReceived(game, gamePackage));

		receiveActions.register(game, events);
		teamReceiveActions.register(game, events);

		PackageCostModifierBehavior.State costModifier = game.state().get(PackageCostModifierBehavior.State.KEY);
		game.state().get(GamePackageState.KEY).addPackageType(data.apply(costModifier));
	}

	private InteractionResult onGamePackageReceived(final IGamePhase game, final GamePackage gamePackage) {
		if (!gamePackage.packageType().equals(data.id())) {
			return InteractionResult.PASS;
		}

		return switch (data.targetSelectionMode()) {
			case SPECIFIC -> receiveSpecific(game, gamePackage);
			case RANDOM -> receiveRandom(game, gamePackage);
			case ALL -> receiveAll(game, gamePackage);
		};
	}

	private InteractionResult receiveSpecific(IGamePhase game, GamePackage gamePackage) {
		if (data.applyToTeam()) {
			TeamState teams = game.instanceState().getOrDefault(TeamState.KEY, TeamState.EMPTY);
			GameTeam receivingTeam = getReceivingTeam(teams, gamePackage);
			if (receivingTeam == null) {
				LOGGER.warn("Could not find a team receiver for package: {}", gamePackage);
				return InteractionResult.FAIL;
			}
			return applyToTeams(game, gamePackage, List.of(receivingTeam), teams.getPlayersForTeam(receivingTeam.key()));
		}

		if (gamePackage.receivingPlayer().isEmpty()) {
			LOGGER.warn("Expected donation package to have a receiver, but did not receive from backend!");
			return InteractionResult.FAIL;
		}

		ServerPlayer receivingPlayer = game.participants().getPlayerBy(gamePackage.receivingPlayer().get());
		if (receivingPlayer == null) {
			// Player not on the server or in the game for some reason
			return InteractionResult.FAIL;
		}
		return applyToPlayers(game, gamePackage, List.of(receivingPlayer));
	}

	@Nullable
	private GameTeam getReceivingTeam(TeamState teams, GamePackage gamePackage) {
		return gamePackage.receivingTeam()
				// Shouldn't happen, but be a bit lenient
				.or(() -> gamePackage.receivingPlayer().map(teams::getTeamForPlayer))
				.map(teams::getTeamByKey)
				.orElse(null);
	}

	private InteractionResult receiveRandom(IGamePhase game, GamePackage gamePackage) {
		if (data.applyToTeam()) {
			TeamState teams = game.instanceState().getOrDefault(TeamState.KEY, TeamState.EMPTY);
			List<GameTeam> allTeams = Lists.newArrayList(teams);
			if (allTeams.isEmpty()) {
				return applyToTeams(game, gamePackage, List.of(), PlayerSet.EMPTY);
			}
			GameTeam randomTeam = Util.getRandom(allTeams, game.random());
			return applyToTeams(game, gamePackage, List.of(randomTeam), teams.getPlayersForTeam(randomTeam.key()));
		} else {
			final ServerPlayer randomPlayer = Util.getRandom(Lists.newArrayList(game.participants()), game.random());
			return applyToPlayers(game, gamePackage, List.of(randomPlayer));
		}
	}

	private InteractionResult receiveAll(IGamePhase game, GamePackage gamePackage) {
		if (data.applyToTeam()) {
			TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
			List<GameTeam> allTeams = teams != null ? Lists.newArrayList(teams) : List.of();
			return applyToTeams(game, gamePackage, allTeams, game.participants());
		} else {
			return applyToPlayers(game, gamePackage, Lists.newArrayList(game.participants()));
		}
	}

	private InteractionResult applyToPlayers(IGamePhase game, GamePackage gamePackage, List<ServerPlayer> players) {
		if (players.isEmpty()) {
			LOGGER.warn("No players to apply package {}, rejecting", gamePackage);
			return InteractionResult.FAIL;
		}
		GameActionContext context = actionContext(gamePackage);
		if (receiveActions.apply(game, context, players)) {
			ServerPlayer singleReceiver = players.size() == 1 ? players.getFirst() : null;
			notification.ifPresent(notification -> notification.onReceive(game, singleReceiver, gamePackage.sendingPlayerName()));
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.FAIL;
	}

	private InteractionResult applyToTeams(IGamePhase game, GamePackage gamePackage, List<GameTeam> teams, PlayerSet players) {
		if (teams.isEmpty()) {
			LOGGER.warn("No teams to apply package {}, rejecting", gamePackage);
			return InteractionResult.FAIL;
		}
		GameActionContext context = actionContext(gamePackage);
		if (teamReceiveActions.apply(game, context, teams) | receiveActions.apply(game, context, players)) {
			GameTeam singleReceiver = teams.size() == 1 ? teams.getFirst() : null;
			notification.ifPresent(notification -> notification.onReceive(game, singleReceiver, gamePackage.sendingPlayerName()));
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.FAIL;
	}

	private static GameActionContext actionContext(GamePackage gamePackage) {
		GameActionContext.Builder context = GameActionContext.builder();
		context.set(GameActionParameter.PACKAGE, gamePackage);
		if (gamePackage.sendingPlayerName() != null) {
			context.set(GameActionParameter.PACKAGE_SENDER, gamePackage.sendingPlayerName());
		}
		return context.build();
	}
}
