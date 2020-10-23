package com.lovetropics.minigames.common.techstack;

import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.game_actions.CarePackageGameAction;
import com.lovetropics.minigames.common.game_actions.PollResultGameAction;
import com.lovetropics.minigames.common.game_actions.GameAction;
import com.lovetropics.minigames.common.game_actions.SabotagePackageGameAction;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public enum BackendRequest
{
	CARE_PACKAGE("care_package", CarePackageGameAction::fromJson, ConfigLT.GENERAL.carePackageDelay::get),
	SABOTAGE_PACKAGE("sabotage_package", SabotagePackageGameAction::fromJson, ConfigLT.GENERAL.sabotagePackageDelay::get),
	CHAT_EVENT("chat_event", PollResultGameAction::fromJson, ConfigLT.GENERAL.chatEventDelay::get);

	public static final BackendRequest[] VALUES = values();

	private final String id;
	private final Function<JsonObject, ? extends GameAction> gameActionFactory;
	private final Supplier<Integer> pollingIntervalSeconds;

	BackendRequest(final String id, final Function<JsonObject, ? extends GameAction> gameActionFactory, final Supplier<Integer> pollingIntervalTicks) {
		this.id = id;
		this.gameActionFactory = gameActionFactory;
		this.pollingIntervalSeconds = pollingIntervalTicks;
	}

	public String getId()
	{
		return id;
	}

	public Function<JsonObject, ? extends GameAction> getGameActionFactory()
	{
		return gameActionFactory;
	}

	public int getPollingIntervalSeconds()
	{
		return pollingIntervalSeconds.get();
	}

	public static Optional<BackendRequest> getFromId(final String id) {
		for (final BackendRequest request : VALUES) {
			if (request.getId().equals(id)) {
				return Optional.of(request);
			}
		}

		return Optional.empty();
	}
}
