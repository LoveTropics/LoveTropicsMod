package com.lovetropics.minigames.common.core.game.behavior.event;

import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;

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


	public static final GameEventType<ApplyPackage> APPLY_PACKAGE = GameEventType.create(ApplyPackage.class, listeners -> (game, player, sendingPlayer) -> {
		for (ApplyPackage listener : listeners) {
			listener.applyPackage(game, player, sendingPlayer);
		}
	});

	private GamePackageEvents() {
	}

	public interface ReceivePackage {
		boolean onReceivePackage(IActiveGame game, GamePackage gamePackage);
	}

	public interface ReceivePollEvent {
		void onReceivePollEvent(IActiveGame game, JsonObject object, String crud);
	}

	public interface ApplyPackage {
		void applyPackage(IActiveGame game, ServerPlayerEntity player, @Nullable String sendingPlayer);
	}
}
