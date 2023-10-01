package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.util.PlayerSnapshot;
import com.lovetropics.minigames.mixin.PlayerListAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;

public final class PlayerIsolation {
	public static final PlayerIsolation INSTANCE = new PlayerIsolation();

	private final Map<UUID, PlayerSnapshot> playerSnapshots = new Object2ObjectOpenHashMap<>();

	private PlayerIsolation() {
	}

	public void accept(ServerPlayer player) {
		if (!this.playerSnapshots.containsKey(player.getUUID())) {
			((PlayerListAccessor) player.getServer().getPlayerList()).ltminigames$save(player);
			PlayerSnapshot snapshot = PlayerSnapshot.takeAndClear(player);
			this.playerSnapshots.put(player.getUUID(), snapshot);
		}
	}

	public void restore(ServerPlayer player) {
		// try to restore the player to their old state
		PlayerSnapshot snapshot = this.playerSnapshots.remove(player.getUUID());
		if (snapshot != null) {
			snapshot.restore(player);
		}
	}

	public boolean isIsolated(ServerPlayer player) {
		return playerSnapshots.containsKey(player.getUUID());
	}
}
