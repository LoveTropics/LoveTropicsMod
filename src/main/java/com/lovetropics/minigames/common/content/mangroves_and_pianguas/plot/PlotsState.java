package com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot;

import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.*;

public final class PlotsState implements Iterable<Plot>, IGameState {
	public static final GameStateKey<PlotsState> KEY = GameStateKey.create("Mangroves & Pianguas Plots");

	private final List<Plot> plots = new ArrayList<>();
	private final Map<UUID, Plot> plotsByPlayer = new Object2ObjectOpenHashMap<>();

	public void addPlayer(ServerPlayerEntity player, Plot plot) {
		this.plotsByPlayer.put(player.getUniqueID(), plot);
		this.plots.add(plot);
	}

	@Nullable
	public Plot removePlayer(ServerPlayerEntity player) {
		Plot plot = this.plotsByPlayer.remove(player.getUniqueID());
		if (plot != null) {
			this.plots.remove(plot);
			return plot;
		} else {
			return null;
		}
	}

	@Nullable
	public Plot getPlotFor(Entity entity) {
		return this.plotsByPlayer.get(entity.getUniqueID());
	}

	@Override
	public Iterator<Plot> iterator() {
		return this.plots.iterator();
	}

	@Nullable
	public Plot getRandomPlot(Random random) {
		if (this.plots.isEmpty()) {
			return null;
		}

		return this.plots.get(random.nextInt(this.plots.size()));
	}
}
