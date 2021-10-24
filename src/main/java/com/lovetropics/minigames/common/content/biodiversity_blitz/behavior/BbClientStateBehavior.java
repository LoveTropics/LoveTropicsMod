package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_tweak.ClientBiodiversityBlitzState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.CurrencyManager;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweak;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.UUID;

public final class BbClientStateBehavior implements IGameBehavior {
	public static final Codec<BbClientStateBehavior> CODEC = Codec.unit(BbClientStateBehavior::new);

	private IGamePhase game;
	private PlotsState plots;

	private final Object2IntMap<UUID> trackedCurrency = new Object2IntOpenHashMap<>();

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;
		this.plots = game.getState().getOrThrow(PlotsState.KEY);

		events.listen(BbEvents.ASSIGN_PLOT, this::addPlayer);
		events.listen(GamePlayerEvents.REMOVE, this::removePlayer);

		events.listen(BbEvents.TICK_PLOT, this::tickPlot);
	}

	private void addPlayer(ServerPlayerEntity player, Plot plot) {
		this.sendOverlayUpdate(player, plot);
	}

	private void removePlayer(ServerPlayerEntity player) {
		GameClientTweak.removeFromPlayer(BiodiversityBlitz.CLIENT_STATE_TWEAK.get(), player);
	}

	private void tickPlot(ServerPlayerEntity player, Plot plot) {
		if (this.game.ticks() % 10 == 0) {
			int currency = CurrencyManager.get(player);
			int lastCurrency = this.trackedCurrency.put(player.getUniqueID(), currency);
			if (currency != lastCurrency) {
				this.sendOverlayUpdate(player, plot);
			}
		}
	}

	private void sendOverlayUpdate(ServerPlayerEntity player, Plot plot) {
		// TODO: display next currency
		int currency = this.trackedCurrency.getInt(player.getUniqueID());

		ClientBiodiversityBlitzState state = new ClientBiodiversityBlitzState(currency, 0);
		GameClientTweak.sendToPlayer(state, player);
	}
}
