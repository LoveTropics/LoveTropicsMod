package com.lovetropics.minigames.common.core.game.behavior.event;

import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;

import java.util.function.Consumer;

import javax.annotation.Nullable;

public final class GamePackageEvents {
	public static final GameEventType<ReceivePackage> RECEIVE_PACKAGE = GameEventType.create(ReceivePackage.class, listeners -> (sendPreamble, gamePackage) -> {
		for (ReceivePackage listener : listeners) {
			ActionResultType result = listener.onReceivePackage(sendPreamble, gamePackage);
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
		boolean applied = false;
		for (ApplyPackage listener : listeners) {
			applied |= listener.applyPackage(player, sendingPlayer);
		}
		return applied;
	});

	private GamePackageEvents() {
	}

	public interface ReceivePackage {
		ActionResultType onReceivePackage(Consumer<IGamePhase> sendPreamble, GamePackage gamePackage);
	}

	public interface ReceivePollEvent {
		void onReceivePollEvent(JsonObject object, String crud);
	}

	public interface ApplyPackage {
		boolean applyPackage(ServerPlayerEntity player, @Nullable String sendingPlayer);
	}
}
