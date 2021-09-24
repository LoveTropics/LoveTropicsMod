package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.instances.control.ControlCommandInvoker;

public final class WaitingGame implements IGamePhase {
	private final GameLobby lobby;

	private final BehaviorMap behaviors;
	private final GameEventListeners events = new GameEventListeners();
	private final GameStateMap state = new GameStateMap();

	private final ControlCommandInvoker controlCommands = ControlCommandInvoker.forGame(this);

	private WaitingGame(GameLobby lobby, BehaviorMap behaviors) {
		this.lobby = lobby;
		this.behaviors = behaviors;
	}

	static GameResult<WaitingGame> create(GameLobby lobby, BehaviorMap behaviors) {
		try {
			WaitingGame waiting = new WaitingGame(lobby, behaviors);

			// TODO: how should waiting state work?
			for (IGameBehavior behavior : waiting.behaviors) {
				behavior.registerState(waiting.state);
			}

			for (IGameBehavior behavior : waiting.behaviors) {
				behavior.registerWaiting(waiting, waiting.events);
			}

			return GameResult.ok(waiting);
		} catch (GameException e) {
			return GameResult.error(e.getTextMessage());
		}
	}

	@Override
	public IGameLobby getLobby() {
		return lobby;
	}

	@Override
	public <T> T invoker(GameEventType<T> type) {
		return events.invoker(type);
	}

	@Override
	public GameStateMap getState() {
		return state;
	}

	@Override
	public ControlCommandInvoker getControlCommands() {
		return controlCommands;
	}
}
