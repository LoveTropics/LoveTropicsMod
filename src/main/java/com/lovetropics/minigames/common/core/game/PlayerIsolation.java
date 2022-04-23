package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.state.IGameState;
import com.lovetropics.minigames.common.core.game.util.PlayerSnapshot;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;

public final class PlayerIsolation implements IGameState {
	private final Map<UUID, PlayerSnapshot> playerSnapshots = new Object2ObjectOpenHashMap<>();

	public void accept(ServerPlayerEntity player) {
		if (!this.playerSnapshots.containsKey(player.getUUID())) {
			PlayerSnapshot snapshot = PlayerSnapshot.takeAndClear(player);
			this.playerSnapshots.put(player.getUUID(), snapshot);
		}
	}

	public void restore(ServerPlayerEntity player) {
		// try to restore the player to their old state
		PlayerSnapshot snapshot = this.playerSnapshots.remove(player.getUUID());
		if (snapshot != null) {
			snapshot.restore(player);
		}
	}
}
