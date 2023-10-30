package com.lovetropics.minigames.common.core.game.behavior.action;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class GameActionParameter<T> {
    public static final GameActionParameter<String> PACKAGE_SENDER = GameActionParameter.create();

    public static final GameActionParameter<ServerPlayer> KILLER = GameActionParameter.create();
    public static final GameActionParameter<ServerPlayer> KILLED = GameActionParameter.create();
    public static final GameActionParameter<ServerPlayer> TARGET = GameActionParameter.create();
    public static final GameActionParameter<Integer> COUNT = GameActionParameter.create();
    public static final GameActionParameter<ItemStack> ITEM = GameActionParameter.create();

    private GameActionParameter() {
    }

    public static <T> GameActionParameter<T> create() {
        return new GameActionParameter<>();
    }
}
