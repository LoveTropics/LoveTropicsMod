package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.state.GamePackageState;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class DonationPackageBehavior implements IGameBehavior {
	public static final Codec<DonationPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				DonationPackageData.CODEC.forGetter(c -> c.data),
				IGameBehavior.CODEC.listOf().fieldOf("receive_behaviors").orElseGet(ArrayList::new).forGetter(c -> c.receiveBehaviors)
		).apply(instance, DonationPackageBehavior::new);
	});

	private static final Logger LOGGER = LogManager.getLogger(DonationPackageBehavior.class);

	public enum PlayerSelect {
		SPECIFIC("specific"), RANDOM("random"), ALL("all");

		public static final Codec<PlayerSelect> CODEC = MoreCodecs.stringVariants(PlayerSelect.values(), s -> s.type);

		public final String type;

		PlayerSelect(final String type) {
			this.type = type;
		}
	}

	private final DonationPackageData data;
	private final List<IGameBehavior> receiveBehaviors;

	private final GameEventListeners applyEvents = new GameEventListeners();

	public DonationPackageBehavior(DonationPackageData data, List<IGameBehavior> receiveBehaviors) {
		this.data = data;
		this.receiveBehaviors = receiveBehaviors;
	}

	public String getPackageType() {
		return data.getPackageType();
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePackageEvents.RECEIVE_PACKAGE, (sendPreamble, gamePackage) -> onGamePackageReceived(sendPreamble, game, gamePackage));

		EventRegistrar receiveEventRegistrar = events.redirect(t -> t == GamePackageEvents.APPLY_PACKAGE_TO_PLAYER || t == GamePackageEvents.APPLY_PACKAGE_GLOBALLY, applyEvents);
		for (IGameBehavior behavior : receiveBehaviors) {
			behavior.register(game, receiveEventRegistrar);
		}

		game.getState().get(GamePackageState.KEY).addPackageType(data.packageType);
	}

	private ActionResultType onGamePackageReceived(Consumer<IGamePhase> sendPreamble, final IGamePhase game, final GamePackage gamePackage) {
		if (!gamePackage.getPackageType().equals(data.packageType)) {
			return ActionResultType.PASS;
		}

		switch (data.playerSelect) {
			case SPECIFIC: return receiveSpecific(sendPreamble, game, gamePackage);
			case RANDOM: return receiveRandom(sendPreamble, game, gamePackage);
			case ALL: return receiveAll(sendPreamble, game, gamePackage);
			default: return ActionResultType.FAIL;
		}
	}

	private ActionResultType receiveSpecific(Consumer<IGamePhase> sendPreamble, IGamePhase game, GamePackage gamePackage) {
		if (gamePackage.getReceivingPlayer() == null) {
			LOGGER.warn("Expected donation package to have a receiving player, but did not receive from backend!");
			return ActionResultType.FAIL;
		}

		ServerPlayerEntity receivingPlayer = game.getParticipants().getPlayerBy(gamePackage.getReceivingPlayer());
		if (receivingPlayer == null) {
			// Player not on the server or in the game for some reason
			return ActionResultType.FAIL;
		}

		String sendingPlayerName = gamePackage.getSendingPlayerName();
		if (applyPackageToPlayer(receivingPlayer, sendingPlayerName)) {
			sendPreamble.accept(game);
			data.onReceive(game, receivingPlayer, sendingPlayerName);

			return ActionResultType.SUCCESS;
		} else {
			return ActionResultType.FAIL;
		}
	}

	private ActionResultType receiveRandom(Consumer<IGamePhase> sendPreamble, IGamePhase game, GamePackage gamePackage) {
		final List<ServerPlayerEntity> players = Lists.newArrayList(game.getParticipants());
		final ServerPlayerEntity randomPlayer = players.get(game.getWorld().getRandom().nextInt(players.size()));

		String sendingPlayerName = gamePackage.getSendingPlayerName();
		if (applyPackageToPlayer(randomPlayer, sendingPlayerName)) {
			sendPreamble.accept(game);
			data.onReceive(game, randomPlayer, sendingPlayerName);

			return ActionResultType.SUCCESS;
		} else {
			return ActionResultType.FAIL;
		}
	}

	private ActionResultType receiveAll(Consumer<IGamePhase> sendPreamble, IGamePhase game, GamePackage gamePackage) {
		String sendingPlayerName = gamePackage.getSendingPlayerName();

		boolean applied = applyPackageGlobally(sendingPlayerName);
		for (ServerPlayerEntity player : game.getParticipants()) {
			applied |= applyPackageToPlayer(player, sendingPlayerName);
		}

		if (!applied) {
			return ActionResultType.FAIL;
		}

		sendPreamble.accept(game);
		data.onReceive(game, null, sendingPlayerName);

		return ActionResultType.SUCCESS;
	}

	private boolean applyPackageToPlayer(final ServerPlayerEntity player, @Nullable final String sendingPlayer) {
		return applyEvents.invoker(GamePackageEvents.APPLY_PACKAGE_TO_PLAYER).applyPackage(player, sendingPlayer);
	}

	private boolean applyPackageGlobally(@Nullable final String sendingPlayer) {
		return applyEvents.invoker(GamePackageEvents.APPLY_PACKAGE_GLOBALLY).applyPackage(sendingPlayer);
	}
}
