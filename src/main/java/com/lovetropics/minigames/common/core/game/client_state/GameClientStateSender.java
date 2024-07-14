package com.lovetropics.minigames.common.core.game.client_state;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.PlayerIsolation;
import com.lovetropics.minigames.common.core.network.SetGameClientStateMessage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = Constants.MODID)
public final class GameClientStateSender {
	private static final GameClientStateSender INSTANCE = new GameClientStateSender();

	private final Map<UUID, PlayerEntry> players = new Object2ObjectOpenHashMap<>();

	public static GameClientStateSender get() {
		return INSTANCE;
	}

	public PlayerEntry byPlayer(ServerPlayer player) {
		return this.players.computeIfAbsent(player.getUUID(), id -> new PlayerEntry());
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			INSTANCE.byPlayer(player).tick(player);
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof final ServerPlayer player && !PlayerIsolation.INSTANCE.isReloading(player)) {
			INSTANCE.players.remove(player.getUUID());
		}
	}

	public static final class PlayerEntry {
		private final Map<GameClientStateType<?>, GameClientState> setQueue = new Object2ObjectOpenHashMap<>();
		private final Set<GameClientStateType<?>> removeQueue = new ObjectOpenHashSet<>();

		public <T extends GameClientState> void enqueueSet(T state) {
			this.removeQueue.remove(state.getType());
			this.setQueue.put(state.getType(), state);
		}

		public <T extends GameClientState> void enqueueRemove(GameClientStateType<T> type) {
			this.setQueue.remove(type);
			this.removeQueue.add(type);
		}

		void tick(ServerPlayer player) {
			if (!setQueue.isEmpty() || !removeQueue.isEmpty()) {
				for (GameClientState state : setQueue.values()) {
					this.sendSet(state, player);
				}

				for (GameClientStateType<?> type : removeQueue) {
					this.sendRemove(type, player);
				}

				setQueue.clear();
				removeQueue.clear();
			}
		}

		void sendSet(GameClientState state, ServerPlayer player) {
			PacketDistributor.sendToPlayer(player, SetGameClientStateMessage.set(state));
		}

		void sendRemove(GameClientStateType<?> type, ServerPlayer player) {
			PacketDistributor.sendToPlayer(player, SetGameClientStateMessage.remove(type));
		}
	}
}
