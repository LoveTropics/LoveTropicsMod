package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.state.GamePackageState;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

public final class DonationPackageBehavior implements IGameBehavior {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final MapCodec<DonationPackageBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			DonationPackageData.CODEC.forGetter(c -> c.data),
			DonationPackageNotification.CODEC.optionalFieldOf("notification").forGetter(c -> c.notification),
			GameActionList.PLAYER_CODEC.optionalFieldOf("receive_actions", GameActionList.EMPTY).forGetter(c -> c.receiveActions)
	).apply(i, DonationPackageBehavior::new));

	private final DonationPackageData data;
	private final Optional<DonationPackageNotification> notification;
	private final GameActionList<ServerPlayer> receiveActions;

	public DonationPackageBehavior(DonationPackageData data, Optional<DonationPackageNotification> notification, GameActionList<ServerPlayer> receiveActions) {
		this.data = data;
		this.notification = notification;
		this.receiveActions = receiveActions;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePackageEvents.RECEIVE_PACKAGE, gamePackage -> onGamePackageReceived(game, gamePackage));

		receiveActions.register(game, events);

		PackageCostModifierBehavior.State costModifier = game.getState().get(PackageCostModifierBehavior.State.KEY);
		game.getState().get(GamePackageState.KEY).addPackageType(data.apply(costModifier));
	}

	private InteractionResult onGamePackageReceived(final IGamePhase game, final GamePackage gamePackage) {
		if (!gamePackage.packageType().equals(data.id())) {
			return InteractionResult.PASS;
		}

		return switch (data.playerSelect()) {
			case SPECIFIC -> receiveSpecific(game, gamePackage);
			case RANDOM -> receiveRandom(game, gamePackage);
			case ALL -> receiveAll(game, gamePackage);
		};
	}

	private InteractionResult receiveSpecific(IGamePhase game, GamePackage gamePackage) {
		if (gamePackage.receivingPlayer().isEmpty()) {
			LOGGER.warn("Expected donation package to have a receiving player, but did not receive from backend!");
			return InteractionResult.FAIL;
		}

		ServerPlayer receivingPlayer = game.getParticipants().getPlayerBy(gamePackage.receivingPlayer().get());
		if (receivingPlayer == null) {
			// Player not on the server or in the game for some reason
			return InteractionResult.FAIL;
		}

		GameActionContext context = actionContext(gamePackage);
		if (receiveActions.apply(game, context, receivingPlayer)) {
			notification.ifPresent(notification -> notification.onReceive(game, receivingPlayer, gamePackage.sendingPlayerName()));

			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.FAIL;
		}
	}

	private InteractionResult receiveRandom(IGamePhase game, GamePackage gamePackage) {
		final List<ServerPlayer> players = Lists.newArrayList(game.getParticipants());
		final ServerPlayer randomPlayer = players.get(game.getWorld().getRandom().nextInt(players.size()));

		GameActionContext context = actionContext(gamePackage);
		if (receiveActions.apply(game, context, randomPlayer)) {
			notification.ifPresent(notification -> notification.onReceive(game, randomPlayer, gamePackage.sendingPlayerName()));

			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.FAIL;
		}
	}

	private InteractionResult receiveAll(IGamePhase game, GamePackage gamePackage) {
		GameActionContext context = actionContext(gamePackage);
		if (!receiveActions.apply(game, context, game.getParticipants())) {
			return InteractionResult.FAIL;
		}

		notification.ifPresent(notification -> notification.onReceive(game, null, gamePackage.sendingPlayerName()));

		return InteractionResult.SUCCESS;
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
