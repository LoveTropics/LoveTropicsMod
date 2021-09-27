package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;

public final class QueuedGame {
	private final IGameDefinition definition;
	private final BehaviorMap behaviors;

	private QueuedGame(IGameDefinition definition, BehaviorMap behaviors) {
		this.definition = definition;
		this.behaviors = behaviors;
	}

	public static QueuedGame create(IGameDefinition game) {
		BehaviorMap behaviors = game.createBehaviors();
		return new QueuedGame(game, behaviors);
	}

	IGameDefinition definition() {
		return this.definition;
	}

	BehaviorMap behaviors() {
		return this.behaviors;
	}
}
