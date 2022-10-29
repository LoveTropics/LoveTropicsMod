package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.state.GamePackageState;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Consumer;

public final class DonationPackageBehavior implements IGameBehavior {
	public static final Codec<DonationPackageBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			DonationPackageData.CODEC.forGetter(c -> c.data),
			MoreCodecs.strictOptionalFieldOf(GameActionList.CODEC, "receive_actions", GameActionList.EMPTY).forGetter(c -> c.receiveActions)
	).apply(i, DonationPackageBehavior::new));

	private static final Logger LOGGER = LogManager.getLogger(DonationPackageBehavior.class);

	public DonationPackageBehavior(DonationPackageData data, GameActionList receiveActions) {
		this.data = data;
		this.receiveActions = receiveActions;
	}

	public enum PlayerSelect {
		SPECIFIC("specific"), RANDOM("random"), ALL("all");

		public static final Codec<PlayerSelect> CODEC = MoreCodecs.stringVariants(PlayerSelect.values(), s -> s.type);

		public final String type;

		PlayerSelect(final String type) {
			this.type = type;
		}
	}

	private final DonationPackageData data;
	private final GameActionList receiveActions;

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePackageEvents.RECEIVE_PACKAGE, (sendPreamble, gamePackage) -> onGamePackageReceived(sendPreamble, game, gamePackage));

		receiveActions.register(game, events);

		game.getState().get(GamePackageState.KEY).addPackageType(data.packageType());
	}

	private InteractionResult onGamePackageReceived(Consumer<IGamePhase> sendPreamble, final IGamePhase game, final GamePackage gamePackage) {
		if (!gamePackage.packageType().equals(data.packageType())) {
			return InteractionResult.PASS;
		}

		return switch (data.playerSelect()) {
			case SPECIFIC -> receiveSpecific(sendPreamble, game, gamePackage);
			case RANDOM -> receiveRandom(sendPreamble, game, gamePackage);
			case ALL -> receiveAll(sendPreamble, game, gamePackage);
		};
	}

	private InteractionResult receiveSpecific(Consumer<IGamePhase> sendPreamble, IGamePhase game, GamePackage gamePackage) {
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
		if (receiveActions.apply(context, receivingPlayer)) {
			sendPreamble.accept(game);
			data.onReceive(game, receivingPlayer, gamePackage.sendingPlayerName());

			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.FAIL;
		}
	}

	private InteractionResult receiveRandom(Consumer<IGamePhase> sendPreamble, IGamePhase game, GamePackage gamePackage) {
		final List<ServerPlayer> players = Lists.newArrayList(game.getParticipants());
		final ServerPlayer randomPlayer = players.get(game.getWorld().getRandom().nextInt(players.size()));

		GameActionContext context = actionContext(gamePackage);
		if (receiveActions.apply(context, randomPlayer)) {
			sendPreamble.accept(game);
			data.onReceive(game, randomPlayer, gamePackage.sendingPlayerName());

			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.FAIL;
		}
	}

	private InteractionResult receiveAll(Consumer<IGamePhase> sendPreamble, IGamePhase game, GamePackage gamePackage) {
		GameActionContext context = actionContext(gamePackage);
		if (!receiveActions.apply(context, game.getParticipants())) {
			return InteractionResult.FAIL;
		}

		sendPreamble.accept(game);
		data.onReceive(game, null, gamePackage.sendingPlayerName());

		return InteractionResult.SUCCESS;
	}

	private static GameActionContext actionContext(GamePackage gamePackage) {
		GameActionContext.Builder context = GameActionContext.builder();
		if (gamePackage.sendingPlayerName() != null) {
			context.set(GameActionParameter.PACKAGE_SENDER, gamePackage.sendingPlayerName());
		}
		return context.build();
	}
}
