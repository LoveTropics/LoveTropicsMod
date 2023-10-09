package com.lovetropics.minigames.common.core.game.behavior.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.mojang.serialization.Codec;

import java.util.List;

public record NoneActionTarget() implements ActionTarget<Void> {
    public static final Codec<NoneActionTarget> CODEC = Codec.unit(NoneActionTarget::new);

    @Override
    public List<Void> resolve(IGamePhase phase, Iterable<Void> sources) {
        return List.of();
    }

    @Override
    public boolean apply(IGamePhase game, GameEventListeners listeners, GameActionContext context, Iterable<Void> sources) {
        return false;
    }

    @Override
    public Codec<? extends ActionTarget<Void>> type() {
        return ActionTargetTypes.NONE.get();
    }
}
