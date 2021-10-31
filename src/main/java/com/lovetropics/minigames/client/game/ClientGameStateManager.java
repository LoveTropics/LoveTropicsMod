package com.lovetropics.minigames.client.game;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.game.handler.ClientGameStateHandlers;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateMap;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class ClientGameStateManager {
	private static GameClientStateMap map;

	public static <T extends GameClientState> void set(T state) {
		GameClientStateMap map = ClientGameStateManager.map;
		if (map == null) {
			ClientGameStateManager.map = map = new GameClientStateMap();
		}

		ClientGameStateHandlers.acceptState(state);
		map.add(state);
	}

	public static <T extends GameClientState> void remove(GameClientStateType<T> type) {
		GameClientStateMap map = ClientGameStateManager.map;
		if (map == null) return;

		T state = map.remove(type);
		if (state != null) {
			ClientGameStateHandlers.disableState(state);

			if (map.isEmpty()) {
				ClientGameStateManager.map = null;
			}
		}
	}

	@Nullable
	public static <T extends GameClientState> T getOrNull(Supplier<GameClientStateType<T>> type) {
		GameClientStateMap map = ClientGameStateManager.map;
		if (map != null) {
			return map.getOrNull(type.get());
		}
		return null;
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		ClientGameStateManager.clearState();
	}

	private static void clearState() {
		GameClientStateMap map = ClientGameStateManager.map;
		if (map != null) {
			for (GameClientState state : map) {
				ClientGameStateHandlers.disableState(state);
			}
		}

		ClientGameStateManager.map = null;
	}
}
