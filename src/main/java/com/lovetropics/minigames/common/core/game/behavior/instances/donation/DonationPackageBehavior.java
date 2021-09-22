package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.state.instances.GamePackageState;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
	public void register(IActiveGame game, EventRegistrar events) {
		events.listen(GamePackageEvents.RECEIVE_PACKAGE, this::onGamePackageReceived);

		EventRegistrar receiveEventRegistrar = events.redirect(t -> t == GamePackageEvents.APPLY_PACKAGE, applyEvents);
		for (IGameBehavior behavior : receiveBehaviors) {
			behavior.register(game, receiveEventRegistrar);
		}

		game.getState().get(GamePackageState.TYPE).addPackageType(data.packageType);
	}

	private boolean onGamePackageReceived(final IActiveGame game, final GamePackage gamePackage) {
		if (!gamePackage.getPackageType().equals(data.packageType)) return false;

		switch (data.playerSelect) {
			case SPECIFIC: return receiveSpecific(game, gamePackage);
			case RANDOM: return receiveRandom(game, gamePackage);
			case ALL: return receiveAll(game, gamePackage);
			default: return false;
		}
	}

	private boolean receiveSpecific(IActiveGame game, GamePackage gamePackage) {
		if (gamePackage.getReceivingPlayer() == null) {
			LOGGER.warn("Expected donation package to have a receiving player, but did not receive from backend!");
			return false;
		}

		ServerPlayerEntity receivingPlayer = game.getParticipants().getPlayerBy(gamePackage.getReceivingPlayer());
		if (receivingPlayer == null) {
			// Player not on the server or in the game for some reason
			return false;
		}

		applyPackage(game, receivingPlayer, gamePackage.getSendingPlayerName());
		data.onReceive(game, receivingPlayer, gamePackage.getSendingPlayerName());

		return true;
	}

	private boolean receiveRandom(IActiveGame game, GamePackage gamePackage) {
		final List<ServerPlayerEntity> players = Lists.newArrayList(game.getParticipants());
		final ServerPlayerEntity randomPlayer = players.get(game.getWorld().getRandom().nextInt(players.size()));

		applyPackage(game, randomPlayer, gamePackage.getSendingPlayerName());
		data.onReceive(game, randomPlayer, gamePackage.getSendingPlayerName());

		return true;
	}

	private boolean receiveAll(IActiveGame game, GamePackage gamePackage) {
		for (ServerPlayerEntity player : game.getParticipants()) {
			applyPackage(game, player, gamePackage.getSendingPlayerName());
		}

		data.onReceive(game, null, gamePackage.getSendingPlayerName());

		return true;
	}

	private void applyPackage(IActiveGame game, final ServerPlayerEntity player, @Nullable final String sendingPlayer) {
		applyEvents.invoker(GamePackageEvents.APPLY_PACKAGE).applyPackage(game, player, sendingPlayer);
	}
}
