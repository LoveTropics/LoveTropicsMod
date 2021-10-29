package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientBiodiversityBlitzState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class BbClientStateBehavior implements IGameBehavior {
	public static final Codec<BbClientStateBehavior> CODEC = Codec.unit(BbClientStateBehavior::new);

	private final Map<UUID, Currency> trackedCurrency = new Object2ObjectOpenHashMap<>();

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(BbEvents.ASSIGN_PLOT, this::addPlayer);
		events.listen(GamePlayerEvents.REMOVE, this::removePlayer);

		events.listen(BbEvents.CURRENCY_CHANGED, (player, value, lastValue) -> {
			this.updateState(player, currency -> currency.value = value);
		});

		events.listen(BbEvents.CURRENCY_INCREMENT_CHANGED, (player, value, lastValue) -> {
			this.updateState(player, currency -> currency.nextIncrement = value);
		});
	}

	private void addPlayer(ServerPlayerEntity player, Plot plot) {
		this.sendStateUpdate(player);
	}

	private void removePlayer(ServerPlayerEntity player) {
		this.trackedCurrency.remove(player.getUniqueID());

		GameClientState.removeFromPlayer(BiodiversityBlitz.CLIENT_STATE.get(), player);
	}

	private void updateState(ServerPlayerEntity player, Consumer<Currency> update) {
		Currency currency = this.trackedCurrency.computeIfAbsent(player.getUniqueID(), uuid -> new Currency());
		update.accept(currency);

		GameClientState.sendToPlayer(currency.asState(), player);
	}

	private void sendStateUpdate(ServerPlayerEntity player) {
		Currency currency = this.trackedCurrency.get(player.getUniqueID());
		if (currency != null) {
			GameClientState.sendToPlayer(currency.asState(), player);
		}
	}

	static final class Currency {
		int value;
		int nextIncrement;

		ClientBiodiversityBlitzState asState() {
			return new ClientBiodiversityBlitzState(this.value, this.nextIncrement);
		}
	}
}
