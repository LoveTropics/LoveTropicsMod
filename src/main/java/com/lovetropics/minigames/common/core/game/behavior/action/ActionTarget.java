package com.lovetropics.minigames.common.core.game.behavior.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.function.Function;

public interface ActionTarget<T> {
    Codec<ActionTarget<?>> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ActionTargetTypes.REGISTRY.get().getCodec())
            .dispatch(ActionTarget::type, Function.identity());
    Codec<ActionTarget<?>> FALLBACK_PLAYER = ExtraCodecs.xor(CODEC, PlayerActionTarget.Target.CODEC)
            .xmap(e -> e.map(Function.identity(), PlayerActionTarget::new), Either::left);

    List<T> resolve(IGamePhase phase, Iterable<T> sources);

    boolean apply(IGamePhase game, GameEventListeners listeners, GameActionContext context, Iterable<T> sources);

    Codec<? extends ActionTarget<T>> type();
}
