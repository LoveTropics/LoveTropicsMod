package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
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
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BbAssignPlotsBehavior implements IGameBehavior {
	public static final Codec<BbAssignPlotsBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Plot.RegionKeys.CODEC.fieldOf("regions").forGetter(c -> c.regionKeys),
			MoreCodecs.arrayOrUnit(Plot.Config.CODEC, Plot.Config[]::new).fieldOf("plots").forGetter(c -> c.plotKeys)
	).apply(instance, BbAssignPlotsBehavior::new));

	private final Plot.RegionKeys regionKeys;

	private final Plot.Config[] plotKeys;
	private final List<Plot> freePlots = new ArrayList<>();

	private PlotsState plots;

	public BbAssignPlotsBehavior(Plot.RegionKeys regionKeys, Plot.Config[] plotKeys) {
		this.regionKeys = regionKeys;
		this.plotKeys = plotKeys;
	}

	@Override
	public void registerState(IGamePhase game, GameStateMap state) {
		plots = state.register(PlotsState.KEY, new PlotsState());
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		MapRegions regions = game.getMapRegions();

		for (Plot.Config config : this.plotKeys) {
			this.freePlots.add(Plot.create(config, this.regionKeys, regions));
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

	private void trySpawnParticipant(IGamePhase game, ServerPlayerEntity player) {
		if (this.freePlots.isEmpty()) {
			game.setPlayerRole(player, PlayerRole.SPECTATOR);
			return;
		}

		Plot plot = this.freePlots.remove(this.freePlots.size() - 1);
		plots.addPlayer(player, plot);

		game.invoker(BbEvents.ASSIGN_PLOT).onAssignPlot(player, plot);
	}
}
