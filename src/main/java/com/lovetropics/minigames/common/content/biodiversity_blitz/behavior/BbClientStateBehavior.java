package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientBbGlobalState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientBbSelfState;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.UUID;
import java.util.function.Consumer;

public final class BbClientStateBehavior implements IGameBehavior {
	public static final Codec<BbClientStateBehavior> CODEC = Codec.unit(BbClientStateBehavior::new);

	private static final int GLOBAL_UPDATE_INTERVAL = 20;

	private final Object2ObjectMap<UUID, Currency> trackedCurrency = new Object2ObjectOpenHashMap<>();

	private boolean globalUpdateQueued;

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.ADD, this::addPlayer);
		events.listen(GamePlayerEvents.REMOVE, this::removePlayer);

		events.listen(BbEvents.CURRENCY_ACCUMULATE, (player, value, lastValue) -> {
			this.updateState(player, currency -> currency.value = value);
		});

		events.listen(BbEvents.CURRENCY_INCREMENT_CHANGED, (player, value, lastValue) -> {
			this.updateState(player, currency -> currency.nextIncrement = value);
		});

		events.listen(GamePhaseEvents.TICK, () -> {
			if (game.ticks() % GLOBAL_UPDATE_INTERVAL == 0 && this.globalUpdateQueued) {
				ClientBbGlobalState state = this.buildGlobalState();
				GameClientState.sendToPlayers(state, game.getAllPlayers());

				this.globalUpdateQueued = false;
			}
		});
	}

	private void addPlayer(ServerPlayerEntity player) {
		GameClientState.sendToPlayer(this.buildGlobalState(), player);
	}

	private void removePlayer(ServerPlayerEntity player) {
		this.trackedCurrency.remove(player.getUniqueID());

		GameClientState.removeFromPlayer(BiodiversityBlitz.SELF_STATE.get(), player);
		GameClientState.removeFromPlayer(BiodiversityBlitz.GLOBAL_STATE.get(), player);
	}

	private void updateState(ServerPlayerEntity player, Consumer<Currency> update) {
		Currency currency = this.trackedCurrency.computeIfAbsent(player.getUniqueID(), uuid -> new Currency());
		update.accept(currency);

		GameClientState.sendToPlayer(
				new ClientBbSelfState(currency.value, currency.nextIncrement),
				player
		);

		this.globalUpdateQueued = true;
	}

	private ClientBbGlobalState buildGlobalState() {
		Object2IntMap<UUID> currency = new Object2IntOpenHashMap<>();
		for (Object2ObjectMap.Entry<UUID, Currency> entry : Object2ObjectMaps.fastIterable(this.trackedCurrency)) {
			currency.put(entry.getKey(), entry.getValue().value);
		}

		return new ClientBbGlobalState(currency);
	}

	static final class Currency {
		int value;
		int nextIncrement;
	}
}
