package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.google.common.collect.Maps;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.PlayerSnapshot;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;

public final class IsolatePlayerStateBehavior implements IMinigameBehavior {
	public static final Codec<IsolatePlayerStateBehavior> CODEC = Codec.unit(IsolatePlayerStateBehavior::new);

	/**
	 * Cache used to know what state the player was in before teleporting into a minigame.
	 */
	private final Map<UUID, PlayerSnapshot> playerSnapshots = Maps.newHashMap();

	@Override
	public void onPlayerJoin(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		if (!this.playerSnapshots.containsKey(player.getUniqueID())) {
			PlayerSnapshot snapshot = PlayerSnapshot.takeAndClear(player);
			this.playerSnapshots.put(player.getUniqueID(), snapshot);
		}
	}

	@Override
	public void onPlayerLeave(IMinigameInstance minigame, ServerPlayerEntity player) {
		// try to restore the player to their old state
		PlayerSnapshot snapshot = this.playerSnapshots.remove(player.getUniqueID());
		if (snapshot != null) {
			snapshot.restore(player);
		}
	}
}
