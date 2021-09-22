package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.IGameDefinition;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Queue;

public final class LobbyGameQueue {
	private final Queue<IGameDefinition> queue = new ArrayDeque<>();

	public void add(IGameDefinition game) {
		queue.add(game);
	}

	@Nullable
	public IGameDefinition next() {
		return queue.poll();
	}
}
