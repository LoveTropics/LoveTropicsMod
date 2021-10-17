package com.lovetropics.minigames.common.core.game.behavior.event;

import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;

import javax.annotation.Nullable;

public final class GamePackageEvents {
	public static final GameEventType<ReceivePackage> RECEIVE_PACKAGE = GameEventType.create(ReceivePackage.class, listeners -> (gamePackage) -> {
		for (ReceivePackage listener : listeners) {
			ActionResultType result = listener.onReceivePackage(gamePackage);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}
		return ActionResultType.FAIL;
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
		ActionResultType onReceivePackage(GamePackage gamePackage);
	}

	public interface ReceivePollEvent {
		void onReceivePollEvent(JsonObject object, String crud);
	}

	public interface ApplyPackage {
		void applyPackage(ServerPlayerEntity player, @Nullable String sendingPlayer);
	}
}
