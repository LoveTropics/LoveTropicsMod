package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.client.lobby.state.ClientBehaviorList;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorList;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

public record QueuedGame(int networkId, IGameDefinition definition, BehaviorList playingBehaviors, @Nullable BehaviorList waitingBehaviors) {
	private static final AtomicInteger NEXT_NETWORK_ID = new AtomicInteger();

	public QueuedGame(int networkId, IGameDefinition definition, BehaviorList playingBehaviors, BehaviorList waitingBehaviors) {
		this.networkId = networkId;
		this.definition = definition;
		this.playingBehaviors = playingBehaviors;
		this.waitingBehaviors = waitingBehaviors;
	}

	public static QueuedGame create(MinecraftServer server, IGameDefinition game) {
		BehaviorList playingBehaviors = game.getPlayingPhase().createBehaviors(server);
		BehaviorList waitingBehaviors = game.getWaitingPhase().map(ph -> ph.createBehaviors(server)).orElse(BehaviorList.EMPTY);
		return new QueuedGame(NEXT_NETWORK_ID.getAndIncrement(), game, playingBehaviors, waitingBehaviors);
	}

	public void configurePlaying(ClientBehaviorList configs) {
		playingBehaviors().configure(configs);
	}

	public void configureWaiting(ClientBehaviorList configs) {
		final BehaviorList behaviors = waitingBehaviors();
		if (behaviors != null) {
			behaviors.configure(configs);
		}
	}
}
