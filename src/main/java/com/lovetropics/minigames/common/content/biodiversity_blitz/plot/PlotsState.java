package com.lovetropics.minigames.common.content.biodiversity_blitz.plot;

import com.google.common.base.Preconditions;
import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public final class PlotsState implements Iterable<Plot>, IGameState {
	public static final GameStateKey<PlotsState> KEY = GameStateKey.create("Biodiversity Blitz Plots");

	private final Set<Plot> plots = new HashSet<>();
	private final Map<UUID, Plot> plotsByPlayer = new Object2ObjectOpenHashMap<>();

	@Nullable
	public Plot getPlotAt(BlockPos pos) {
		for (Plot plot : plots) {
			if (plot.walls.containsBlock(pos)) {
				return plot;
			}
		}
		return null;
	}

	public void addPlayer(ServerPlayer player, Plot plot) {
		Preconditions.checkArgument(plot != null, "Plot must never be null");

		this.plotsByPlayer.put(player.getUUID(), plot);
		this.plots.add(plot);
	}

	@Nullable
	public Plot removePlayer(ServerPlayer player) {
		Plot plot = this.plotsByPlayer.remove(player.getUUID());
		if (plot != null) {
			if (!this.plotsByPlayer.containsValue(plot)) {
				this.plots.remove(plot);
			}

			return plot;
		} else {
			return null;
		}
	}

	@Nullable
	public Plot getPlotFor(Entity entity) {
		return this.plotsByPlayer.get(entity.getUUID());
	}

	@Override
	@NotNull
	public Iterator<Plot> iterator() {
		return this.plots.iterator();
	}

	@Nullable
	public Plot getRandomPlot(RandomSource random) {
		if (this.plots.isEmpty()) {
			return null;
		}

		List<Plot> plots = new ArrayList<>(this.plots);

		return plots.get(random.nextInt(plots.size()));
	}
}
