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

	private QueuedGame(int networkId, IGameDefinition definition, BehaviorMap playingBehaviors) {
		this.networkId = networkId;
		this.definition = definition;
		this.playingBehaviors = playingBehaviors;
	}

	public static QueuedGame create(MinecraftServer server, IGameDefinition game) {
		// TODO: integrate waiting behaviors
		BehaviorMap playingBehaviors = game.getPlayingPhase().createBehaviors(server);
		return new QueuedGame(NEXT_NETWORK_ID.getAndIncrement(), game, playingBehaviors);
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
}
