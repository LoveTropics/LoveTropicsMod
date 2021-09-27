package com.lovetropics.minigames.common.core.game.behavior.event;

import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;

public final class GamePackageEvents {
	public static final GameEventType<ReceivePackage> RECEIVE_PACKAGE = GameEventType.create(ReceivePackage.class, listeners -> (gamePackage) -> {
		boolean handled = false;
		for (ReceivePackage listener : listeners) {
			handled |= listener.onReceivePackage(gamePackage);
		}
		return handled;
	});

	public static final GameEventType<ReceivePollEvent> RECEIVE_POLL_EVENT = GameEventType.create(ReceivePollEvent.class, listeners -> (object, crud) -> {
		for (ReceivePollEvent listener : listeners) {
			listener.onReceivePollEvent(object, crud);
		}
	});


	public static final GameEventType<ApplyPackage> APPLY_PACKAGE = GameEventType.create(ApplyPackage.class, listeners -> (player, sendingPlayer) -> {
		for (ApplyPackage listener : listeners) {
			listener.applyPackage(player, sendingPlayer);
		}
	});

	private GamePackageEvents() {
	}

	public interface ReceivePackage {
		boolean onReceivePackage(GamePackage gamePackage);
	}

	public interface ReceivePollEvent {
		void onReceivePollEvent(JsonObject object, String crud);
	}

	public interface ApplyPackage {
		void applyPackage(ServerPlayerEntity player, @Nullable String sendingPlayer);
	}
}
