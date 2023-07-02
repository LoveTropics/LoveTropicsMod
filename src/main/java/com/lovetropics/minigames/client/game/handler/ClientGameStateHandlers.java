package com.lovetropics.minigames.client.game.handler;

import com.lovetropics.minigames.client.game.handler.spectate.ClientSpectatingManager;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.tterrag.registrate.util.entry.RegistryEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.Map;

public final class ClientGameStateHandlers {
	private static final Map<ResourceLocation, ClientGameStateHandler<?>> REGISTRY = new Object2ObjectOpenHashMap<>();

	static {
		register(GameClientStateTypes.SPECTATING, ClientSpectatingManager.INSTANCE);
		register(GameClientStateTypes.RESOURCE_PACK, GameResourcePackHandler.INSTANCE);
	}

	public static <T extends GameClientState> void register(RegistryEntry<GameClientStateType<T>> type, ClientGameStateHandler<T> handler) {
		REGISTRY.put(type.getId(), handler);
	}

	public static <T extends GameClientState> void register(RegistryObject<GameClientStateType<T>> type, ClientGameStateHandler<T> handler) {
		REGISTRY.put(type.getId(), handler);
	}

	public static <T extends GameClientState> void acceptState(T state) {
		ClientGameStateHandler<T> handler = ClientGameStateHandlers.get(state);
		if (handler != null) {
			handler.accept(state);
		}
	}

	public static <T extends GameClientState> void disableState(T state) {
		ClientGameStateHandler<T> handler = ClientGameStateHandlers.get(state);
		if (handler != null) {
			handler.disable(state);
		}
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends GameClientState> ClientGameStateHandler<T> get(T state) {
		ResourceLocation id = GameClientStateTypes.REGISTRY.get().getKey(state.getType());
		if (id != null) {
			return (ClientGameStateHandler<T>) REGISTRY.get(id);
		} else {
			return null;
		}
	}
}
