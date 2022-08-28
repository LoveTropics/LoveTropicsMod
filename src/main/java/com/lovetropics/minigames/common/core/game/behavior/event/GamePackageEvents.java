package com.lovetropics.minigames.common.core.game.behavior.event;

import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

import java.util.function.Consumer;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public final class GamePackageEvents {
	public static final GameEventType<ReceivePackage> RECEIVE_PACKAGE = GameEventType.create(ReceivePackage.class, listeners -> (sendPreamble, gamePackage) -> {
		for (ReceivePackage listener : listeners) {
			InteractionResult result = listener.onReceivePackage(sendPreamble, gamePackage);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResult.FAIL;
	});

	public static final GameEventType<ReceivePollEvent> RECEIVE_POLL_EVENT = GameEventType.create(ReceivePollEvent.class, listeners -> (object, crud) -> {
		for (ReceivePollEvent listener : listeners) {
			listener.onReceivePollEvent(object, crud);
		}
	});

	private GamePackageEvents() {
	}

	public interface ReceivePackage {
		InteractionResult onReceivePackage(Consumer<IGamePhase> sendPreamble, GamePackage gamePackage);
	}

	public interface ReceivePollEvent {
		void onReceivePollEvent(JsonObject object, String crud);
	}
}
