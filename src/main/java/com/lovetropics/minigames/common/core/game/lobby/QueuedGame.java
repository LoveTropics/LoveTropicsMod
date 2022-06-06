package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.client.lobby.state.ClientBehaviorMap;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

public record QueuedGame(int networkId, IGameDefinition definition, BehaviorMap playingBehaviors, @Nullable BehaviorMap waitingBehaviors) {
	private static final AtomicInteger NEXT_NETWORK_ID = new AtomicInteger();

	public QueuedGame(int networkId, IGameDefinition definition, BehaviorMap playingBehaviors, BehaviorMap waitingBehaviors) {
		this.networkId = networkId;
		this.definition = definition;
		this.playingBehaviors = playingBehaviors;
		this.waitingBehaviors = waitingBehaviors;
	}

	public static QueuedGame create(MinecraftServer server, IGameDefinition game) {
		BehaviorMap playingBehaviors = game.getPlayingPhase().createBehaviors(server);
		BehaviorMap waitingBehaviors = game.getWaitingPhase().map(ph -> ph.createBehaviors(server)).orElse(BehaviorMap.EMPTY);
		return new QueuedGame(NEXT_NETWORK_ID.getAndIncrement(), game, playingBehaviors, waitingBehaviors);
	}

	public void configurePlaying(ClientBehaviorMap configs) {
		playingBehaviors().configure(configs);
	}

	public void configureWaiting(ClientBehaviorMap configs) {
		final BehaviorMap behaviors = waitingBehaviors();
		if (behaviors != null) {
			behaviors.configure(configs);
		}
	}
}
