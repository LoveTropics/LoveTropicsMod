package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.atomic.AtomicInteger;

public final class QueuedGame {
	private static final AtomicInteger NEXT_NETWORK_ID = new AtomicInteger();

	private final int networkId;
	private final IGameDefinition definition;
	private final BehaviorMap playingBehaviors;
	private final BehaviorMap waitingBehaviors;

	private QueuedGame(int networkId, IGameDefinition definition, BehaviorMap playingBehaviors, BehaviorMap waitingBehaviors) {
		this.networkId = networkId;
		this.definition = definition;
		this.playingBehaviors = playingBehaviors;
		this.waitingBehaviors = waitingBehaviors;
	}

	public static QueuedGame create(MinecraftServer server, IGameDefinition game) {
		BehaviorMap playingBehaviors = game.getPlayingPhase().createBehaviors(server);
		BehaviorMap waitingBehaviors = game.getWaitingPhase().createBehaviors(server);
		return new QueuedGame(NEXT_NETWORK_ID.getAndIncrement(), game, playingBehaviors, waitingBehaviors);
	}

	public int networkId() {
		return networkId;
	}

	public IGameDefinition definition() {
		return definition;
	}

	public BehaviorMap playingBehaviors() {
		return this.playingBehaviors;
	}

	public BehaviorMap waitingBehaviors() {
		return this.waitingBehaviors;
	}
}
