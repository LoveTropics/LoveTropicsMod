package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.google.common.collect.Maps;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.PlayerSnapshot;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;

public final class IsolatePlayerStateBehavior implements IGameBehavior {
	public static final Codec<IsolatePlayerStateBehavior> CODEC = Codec.unit(IsolatePlayerStateBehavior::new);

	/**
	 * Cache used to know what state the player was in before teleporting into a minigame.
	 */
	private final Map<UUID, PlayerSnapshot> playerSnapshots = Maps.newHashMap();

	@Override
	public void onPlayerJoin(IGameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		if (!this.playerSnapshots.containsKey(player.getUniqueID())) {
			PlayerSnapshot snapshot = PlayerSnapshot.takeAndClear(player);
			this.playerSnapshots.put(player.getUniqueID(), snapshot);
		}
	}

	@Override
	public void onPlayerLeave(IGameInstance minigame, ServerPlayerEntity player) {
		// try to restore the player to their old state
		PlayerSnapshot snapshot = this.playerSnapshots.remove(player.getUniqueID());
		if (snapshot != null) {
			snapshot.restore(player);
		}
	}
}