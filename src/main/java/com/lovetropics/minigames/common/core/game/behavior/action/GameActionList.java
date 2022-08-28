package com.lovetropics.minigames.common.core.game.behavior.action;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.List;

public class GameActionList {
    public static final GameActionList EMPTY = new GameActionList(List.of());

    public static final Codec<GameActionList> CODEC = MoreCodecs.listOrUnit(IGameBehavior.CODEC).xmap(GameActionList::new, c -> c.behaviors);

    private final List<IGameBehavior> behaviors;

    private final GameEventListeners listeners = new GameEventListeners();

    public GameActionList(List<IGameBehavior> behaviors) {
        this.behaviors = behaviors;
    }

    public void register(IGamePhase game, EventRegistrar events) {
        for (IGameBehavior behavior : behaviors) {
            behavior.register(game, events.redirect(GameActionEvents::matches, listeners));
        }
    }

    public boolean apply(GameActionContext context, ServerPlayer... targets) {
        return apply(context, Arrays.asList(targets));
    }

    public boolean apply(GameActionContext context, Iterable<ServerPlayer> targets) {
        boolean result = listeners.invoker(GameActionEvents.APPLY).apply(context, targets);
        for (ServerPlayer target : targets) {
            result |= listeners.invoker(GameActionEvents.APPLY_TO_PLAYER).apply(context, target);
        }
        return result;
    }
}
