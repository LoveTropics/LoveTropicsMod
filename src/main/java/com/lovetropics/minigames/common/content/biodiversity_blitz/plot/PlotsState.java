package com.lovetropics.minigames.common.content.biodiversity_blitz.plot;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PlotsState implements Iterable<Plot>, IGameState {
	public static final GameStateKey<PlotsState> KEY = GameStateKey.create("Biodiversity Blitz Plots");

	private final Set<Plot> plots = new HashSet<>();
	private final Map<UUID, Plot> plotsByPlayer = new Object2ObjectOpenHashMap<>();
	private final SetMultimap<Plot, UUID> plotToPlayers = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);

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
		this.plotToPlayers.put(plot, player.getUUID());
	}

	@Nullable
	public Plot removePlayer(ServerPlayer player) {
		Plot plot = this.plotsByPlayer.remove(player.getUUID());
		if (plot != null) {
			if (!this.plotsByPlayer.containsValue(plot)) {
				this.plots.remove(plot);
				this.plotToPlayers.remove(plot, player.getUUID());
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

	public Set<UUID> getPlayersInPlot(Plot plot) {
		return plotToPlayers.get(plot);
	}

	@Override
	@NotNull
	public Iterator<Plot> iterator() {
		return this.plots.iterator();
	}

	@Nullable
	public Plot getRandomPlot(RandomSource random) {
		if (plots.isEmpty()) {
			return null;
		}
		List<Plot> plots = List.copyOf(this.plots);
		return Util.getRandom(plots, random);
	}
}
