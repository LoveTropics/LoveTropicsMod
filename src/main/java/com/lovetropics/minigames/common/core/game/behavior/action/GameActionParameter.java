package com.lovetropics.minigames.common.core.game.behavior.action;

public class GameActionParameter<T> {
    public static final GameActionParameter<String> PACKAGE_SENDER = GameActionParameter.create();

    private GameActionParameter() {
    }

    public static <T> GameActionParameter<T> create() {
        return new GameActionParameter<>();
    }
}
