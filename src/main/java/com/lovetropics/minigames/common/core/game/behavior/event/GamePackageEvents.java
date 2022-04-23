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

	public static final GameEventType<ApplyPackageToPlayer> APPLY_PACKAGE_TO_PLAYER = GameEventType.create(ApplyPackageToPlayer.class, listeners -> (player, sendingPlayer) -> {
		boolean applied = false;
		for (ApplyPackageToPlayer listener : listeners) {
			applied |= listener.applyPackage(player, sendingPlayer);
		}
		return applied;
	});

	public static final GameEventType<ApplyPackageGlobally> APPLY_PACKAGE_GLOBALLY = GameEventType.create(ApplyPackageGlobally.class, listeners -> sendingPlayer -> {
		boolean applied = false;
		for (ApplyPackageGlobally listener : listeners) {
			applied |= listener.applyPackage(sendingPlayer);
		}
		return applied;
	});

	private GamePackageEvents() {
	}

	public interface ReceivePackage {
		InteractionResult onReceivePackage(Consumer<IGamePhase> sendPreamble, GamePackage gamePackage);
	}

	public interface ReceivePollEvent {
		void onReceivePollEvent(JsonObject object, String crud);
	}

	public interface ApplyPackageToPlayer {
		boolean applyPackage(ServerPlayer player, @Nullable String sendingPlayer);
	}

	public interface ApplyPackageGlobally {
		boolean applyPackage(@Nullable String sendingPlayer);
	}
}
