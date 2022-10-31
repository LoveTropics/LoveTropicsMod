package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import net.minecraft.server.level.ServerPlayer;

public final class GameActionEvents {
    public static final GameEventType<Apply> APPLY = GameEventType.create(Apply.class, listeners -> (context, sources) -> {
        boolean applied = false;
        for (Apply listener : listeners) {
            applied |= listener.apply(context, sources);
        }
        return applied;
    });

    public static final GameEventType<ApplyToPlayer> APPLY_TO_PLAYER = GameEventType.create(ApplyToPlayer.class, listeners -> (context, target) -> {
        boolean applied = false;
        for (ApplyToPlayer listener : listeners) {
            applied |= listener.apply(context, target);
        }
        return applied;
    });

    private GameActionEvents() {
    }

    public static boolean matches(GameEventType<?> type) {
        return type == APPLY || type == APPLY_TO_PLAYER;
    }

    public interface ApplyToPlayer {
        boolean apply(GameActionContext context, ServerPlayer target);
    }

    public interface Apply {
        boolean apply(GameActionContext context, Iterable<ServerPlayer> sources);
    }
}
