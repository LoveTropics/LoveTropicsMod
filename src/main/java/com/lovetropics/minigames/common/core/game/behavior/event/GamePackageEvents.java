package com.lovetropics.minigames.common.core.game.behavior.event;

import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;

public final class GamePackageEvents {
	public static final GameEventType<ReceivePackage> RECEIVE_PACKAGE = GameEventType.create(ReceivePackage.class, listeners -> (game, gamePackage) -> {
		boolean handled = false;
		for (ReceivePackage listener : listeners) {
			handled |= listener.onReceivePackage(game, gamePackage);
		}
		return handled;
	});

	public static final GameEventType<ReceivePollEvent> RECEIVE_POLL_EVENT = GameEventType.create(ReceivePollEvent.class, listeners -> (game, object, crud) -> {
		for (ReceivePollEvent listener : listeners) {
			listener.onReceivePollEvent(game, object, crud);
		}
	});

	private GamePackageEvents() {
	}

	public interface ReceivePackage {
		boolean onReceivePackage(IGameInstance game, GamePackage gamePackage);
	}

	public interface ReceivePollEvent {
		void onReceivePollEvent(IGameInstance game, JsonObject object, String crud);
	}
}
