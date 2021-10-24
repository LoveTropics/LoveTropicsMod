package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_tweak.ClientBiodiversityBlitzState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.CurrencyManager;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweak;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;

public final class BbClientStateBehavior implements IGameBehavior {
	public static final Codec<BbClientStateBehavior> CODEC = Codec.unit(BbClientStateBehavior::new);

	private IGamePhase game;

	private final Map<UUID, Currency> trackedCurrency = new Object2ObjectOpenHashMap<>();

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;

		events.listen(BbEvents.ASSIGN_PLOT, this::addPlayer);
		events.listen(GamePlayerEvents.REMOVE, this::removePlayer);

		events.listen(BbEvents.TICK_PLOT, this::tickPlot);
	}

	private void addPlayer(ServerPlayerEntity player, Plot plot) {
		this.sendOverlayUpdate(player);
	}

	private void removePlayer(ServerPlayerEntity player) {
		this.trackedCurrency.remove(player.getUniqueID());

		GameClientTweak.removeFromPlayer(BiodiversityBlitz.CLIENT_STATE_TWEAK.get(), player);
	}

	private void tickPlot(ServerPlayerEntity player, Plot plot) {
		if (this.game.ticks() % 10 == 0) {
			this.tickTrackedCurrency(player, plot);
		}
	}

	private void tickTrackedCurrency(ServerPlayerEntity player, Plot plot) {
		Currency trackedCurrency = this.trackedCurrency.computeIfAbsent(player.getUniqueID(), uuid -> new Currency());

		if (trackedCurrency.set(CurrencyManager.get(player), plot.nextCurrencyIncrement)) {
			this.sendOverlayUpdate(player);
		}
	}

	private void sendOverlayUpdate(ServerPlayerEntity player) {
		Currency currency = this.trackedCurrency.get(player.getUniqueID());
		if (currency != null) {
			GameClientTweak.sendToPlayer(currency.asStateTweak(), player);
		}
	}

	static final class Currency {
		int value;
		int nextIncrement;

		boolean set(int value, int nextIncrement) {
			if (this.value == value && this.nextIncrement == nextIncrement) {
				return false;
			}

			this.value = value;
			this.nextIncrement = nextIncrement;
			return true;
		}

		ClientBiodiversityBlitzState asStateTweak() {
			return new ClientBiodiversityBlitzState(this.value, this.nextIncrement);
		}
	}
}
