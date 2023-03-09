package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.tutorial.TutorialState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.CheckeredPlotsState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BbAssignPlotsBehavior implements IGameBehavior {
	public static final Codec<BbAssignPlotsBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			Plot.RegionKeys.CODEC.fieldOf("regions").forGetter(c -> c.regionKeys),
			MoreCodecs.arrayOrUnit(Plot.Config.CODEC, Plot.Config[]::new).fieldOf("plots").forGetter(c -> c.plotKeys),
			Codec.BOOL.fieldOf("teams_mode").orElse(false).forGetter(c -> c.teamsMode)
	).apply(i, BbAssignPlotsBehavior::new));

	private final Plot.RegionKeys regionKeys;

	private final Plot.Config[] plotKeys;
	private final boolean teamsMode;
	private final List<Plot> freePlots = new ArrayList<>();
	private int usedPlotIdx;

	private PlotsState plots;

	public BbAssignPlotsBehavior(Plot.RegionKeys regionKeys, Plot.Config[] plotKeys, boolean teamsMode) {
		this.regionKeys = regionKeys;
		this.plotKeys = plotKeys;
		this.teamsMode = teamsMode;
	}

	@Override
	public void registerState(IGamePhase game, GameStateMap phaseState, GameStateMap instanceState) {
		plots = phaseState.register(PlotsState.KEY, new PlotsState());
		// TODO: find a better place for this
		phaseState.register(TutorialState.KEY, new TutorialState());
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		MapRegions regions = game.getMapRegions();

		for (Plot.Config config : this.plotKeys) {
			this.freePlots.add(Plot.create(game.getWorld(), config, this.regionKeys, regions));
		}

		this.applyCheckeredPlots(events);

		Collections.shuffle(this.freePlots);

		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> {
			if (role == PlayerRole.PARTICIPANT) {
				trySpawnParticipant(game, player);
			}
		});

		events.listen(GamePlayerEvents.REMOVE, plots::removePlayer);
	}

	private void applyCheckeredPlots(EventRegistrar events) {
		CheckeredPlotsState checkeredPlots = new CheckeredPlotsState(
				this.freePlots.stream().map(plot -> plot.bounds).toArray(BlockBox[]::new)
		);

		GameClientState.applyGlobally(checkeredPlots, events);
	}

	private void trySpawnParticipant(IGamePhase game, ServerPlayer player) {
		if (this.freePlots.isEmpty()) {
			game.setPlayerRole(player, PlayerRole.SPECTATOR);
			return;
		}

		if (teamsMode) {
			// Simple, temporary round robin style player assignment
			// TODO- proper teams selection
			usedPlotIdx = (usedPlotIdx + 1) % freePlots.size();

			Plot plot = this.freePlots.get(usedPlotIdx);
			plots.addPlayer(player, plot);

			game.invoker(BbEvents.ASSIGN_PLOT).onAssignPlot(player, plot);
			return;
		}

		if (plots.getPlotFor(player) == null) {
			Plot plot = this.freePlots.remove(this.freePlots.size() - 1);
			plots.addPlayer(player, plot);
	
			game.invoker(BbEvents.ASSIGN_PLOT).onAssignPlot(player, plot);
		}
	}
}
