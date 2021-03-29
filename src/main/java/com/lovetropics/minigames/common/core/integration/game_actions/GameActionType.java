package com.lovetropics.minigames.common.core.integration.game_actions;

import com.lovetropics.minigames.common.config.ConfigLT;
import com.mojang.serialization.Codec;

import java.util.Optional;
import java.util.function.Supplier;

public enum GameActionType {
	DONATION_PACKAGE("donation_package", DonationPackageGameAction.CODEC, ConfigLT.GENERAL.donationPackageDelay::get),
	CHAT_EVENT("chat_event", ChatEventGameAction.CODEC, ConfigLT.GENERAL.chatEventDelay::get);

	public static final GameActionType[] VALUES = values();

	private final String id;
	private final Codec<? extends GameAction> codec;
	private final Supplier<Integer> pollingIntervalSeconds;

	GameActionType(final String id, final Codec<? extends GameAction> codec, final Supplier<Integer> pollingIntervalTicks) {
		this.id = id;
		this.codec = codec;
		this.pollingIntervalSeconds = pollingIntervalTicks;
	}

	public String getId() {
		return id;
	}

	public Codec<? extends GameAction> getCodec() {
		return codec;
	}

	public int getPollingIntervalSeconds() {
		return pollingIntervalSeconds.get();
	}

	public int getPollingIntervalTicks() {
		return getPollingIntervalSeconds() * 20;
	}

	public static Optional<GameActionType> getFromId(final String id) {
		for (final GameActionType request : VALUES) {
			if (request.getId().equals(id)) {
				return Optional.of(request);
			}
		}

		return Optional.empty();
	}
}
