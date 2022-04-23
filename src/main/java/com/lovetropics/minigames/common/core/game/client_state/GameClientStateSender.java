package com.lovetropics.minigames.common.core.game.client_state;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.SetGameClientStateMessage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Constants.MODID)
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
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer) {
			ServerPlayer player = (ServerPlayer) event.player;
			INSTANCE.byPlayer(player).tick(player);
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		Player player = event.getPlayer();
		INSTANCE.players.remove(player.getUUID());
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
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), SetGameClientStateMessage.set(state));
		}

		void sendRemove(GameClientStateType<?> type, ServerPlayer player) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), SetGameClientStateMessage.remove(type));
		}
	}
}
