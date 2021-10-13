package com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot;

import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class PlotsState implements Iterable<Plot>, IGameState {
	public static final GameStateKey<PlotsState> KEY = GameStateKey.create("Mangroves & Pianguas Plots");

	private final Map<UUID, Plot> plotsByPlayer = new Object2ObjectOpenHashMap<>();

	public void addPlayer(ServerPlayerEntity player, Plot plot) {
		this.plotsByPlayer.put(player.getUniqueID(), plot);
	}

	@Nullable
	public Plot removePlayer(ServerPlayerEntity player) {
		return this.plotsByPlayer.remove(player.getUniqueID());
	}

	@Nullable
	public Plot getPlotFor(Entity entity) {
		return this.plotsByPlayer.get(entity.getUniqueID());
	}

	@Override
	public Iterator<Plot> iterator() {
		return this.plotsByPlayer.values().iterator();
	}
}
