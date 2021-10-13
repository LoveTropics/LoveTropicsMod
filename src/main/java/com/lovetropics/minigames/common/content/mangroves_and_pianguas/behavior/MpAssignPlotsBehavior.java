package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.PlotsState;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MpAssignPlotsBehavior implements IGameBehavior {
	public static final Codec<MpAssignPlotsBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			MoreCodecs.arrayOrUnit(Plot.Keys.CODEC, Plot.Keys[]::new).optionalFieldOf("plots", new Plot.Keys[0]).forGetter(c -> c.plotKeys)
	).apply(instance, MpAssignPlotsBehavior::new));

	private final Plot.Keys[] plotKeys;
	private final List<Plot> freePlots = new ArrayList<>();

	private PlotsState plots;

	public MpAssignPlotsBehavior(Plot.Keys[] plotKeys) {
		this.plotKeys = plotKeys;
	}

	@Override
	public void registerState(IGamePhase game, GameStateMap state) {
		plots = state.register(PlotsState.KEY, new PlotsState());
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		MapRegions regions = game.getMapRegions();

		for (Plot.Keys keys : this.plotKeys) {
			this.freePlots.add(Plot.associate(keys, regions));
		}

		Collections.shuffle(this.freePlots);

		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> {
			if (role == PlayerRole.PARTICIPANT) {
				trySpawnParticipant(game, player);
			}
		});

		events.listen(GamePlayerEvents.REMOVE, plots::removePlayer);
	}

	private void trySpawnParticipant(IGamePhase game, ServerPlayerEntity player) {
		if (this.freePlots.isEmpty()) {
			game.setPlayerRole(player, PlayerRole.SPECTATOR);
			return;
		}

		Plot plot = this.freePlots.remove(this.freePlots.size() - 1);
		plots.addPlayer(player, plot);

		game.invoker(MpEvents.ASSIGN_PLOT).onAssignPlot(player, plot);
	}
}