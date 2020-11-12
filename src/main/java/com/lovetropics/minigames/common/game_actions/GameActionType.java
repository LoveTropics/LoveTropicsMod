package com.lovetropics.minigames.common.game_actions;

import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.config.ConfigLT;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public enum GameActionType {
	DONATION_PACKAGE("donation_package", DonationPackageGameAction::fromJson, ConfigLT.GENERAL.donationPackageDelay::get),
	CHAT_EVENT("chat_event", ChatEventGameAction::fromJson, ConfigLT.GENERAL.chatEventDelay::get);

	public static final GameActionType[] VALUES = values();

	private final String id;
	private final Function<JsonObject, ? extends GameAction> gameActionFactory;
	private final Supplier<Integer> pollingIntervalSeconds;

	GameActionType(final String id, final Function<JsonObject, ? extends GameAction> gameActionFactory, final Supplier<Integer> pollingIntervalTicks) {
		this.id = id;
		this.gameActionFactory = gameActionFactory;
		this.pollingIntervalSeconds = pollingIntervalTicks;
	}

	public String getId() {
		return id;
	}

	public GameAction createAction(JsonObject object) {
		return gameActionFactory.apply(object);
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
