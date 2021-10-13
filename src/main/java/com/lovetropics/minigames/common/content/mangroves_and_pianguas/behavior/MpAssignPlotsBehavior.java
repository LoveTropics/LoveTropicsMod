package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.state.MpPlot;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.state.MpPlotsState;
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
			MoreCodecs.arrayOrUnit(MpPlot.Keys.CODEC, MpPlot.Keys[]::new).optionalFieldOf("plots", new MpPlot.Keys[0]).forGetter(c -> c.plotKeys)
	).apply(instance, MpAssignPlotsBehavior::new));

	private final MpPlot.Keys[] plotKeys;
	private final List<MpPlot> freePlots = new ArrayList<>();

	private MpPlotsState plots;

	public MpAssignPlotsBehavior(MpPlot.Keys[] plotKeys) {
		this.plotKeys = plotKeys;
	}

	@Override
	public void registerState(IGamePhase game, GameStateMap state) {
		plots = state.register(MpPlotsState.KEY, new MpPlotsState());
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		MapRegions regions = game.getMapRegions();

		for (MpPlot.Keys keys : this.plotKeys) {
			this.freePlots.add(MpPlot.associate(keys, regions));
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

		MpPlot plot = this.freePlots.remove(this.freePlots.size() - 1);
		plots.addPlayer(player, plot);

		game.invoker(MpEvents.ASSIGN_PLOT).onAssignPlot(player, plot);
	}
}
