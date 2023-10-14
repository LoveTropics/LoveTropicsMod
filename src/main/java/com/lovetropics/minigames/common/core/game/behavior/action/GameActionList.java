package com.lovetropics.minigames.common.core.game.behavior.action;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class GameActionList<T> {
    public static final GameActionList<ServerPlayer> EMPTY = new GameActionList<>(List.of(), PlayerActionTarget.SOURCE);

    public static final MapCodec<GameActionList<?>> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            MoreCodecs.listOrUnit(IGameBehavior.CODEC).fieldOf("actions").forGetter(list -> list.behaviors),
            ActionTarget.FALLBACK_PLAYER.optionalFieldOf("target", PlayerActionTarget.SOURCE).forGetter(list -> list.target)
    ).apply(i, GameActionList::new));


    public static final Codec<GameActionList<ServerPlayer>> PLAYER = codec(ActionTargetTypes.PLAYER, PlayerActionTarget.SOURCE);
    public static final Codec<GameActionList<Void>> VOID = codec(ActionTargetTypes.NONE, NoneActionTarget.INSTANCE);


    public static <T, A extends ActionTarget<T>> MapCodec<GameActionList<T>> mapCodec(Supplier<Codec<A>> type, A target) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                MoreCodecs.listOrUnit(IGameBehavior.CODEC).fieldOf("actions").forGetter(list -> list.behaviors),
                ExtraCodecs.lazyInitializedCodec(type).optionalFieldOf("target", target).forGetter(list -> (A) list.target)
        ).apply(i, GameActionList::new));
    }

    public static <T, A extends ActionTarget<T>> Codec<GameActionList<T>> codec(Supplier<Codec<A>> type, A target) {
        var simpleCodec = MoreCodecs.listOrUnit(IGameBehavior.CODEC)
                .flatComapMap(
                        behaviors -> new GameActionList<>(behaviors, target),
                        list -> {
                            if (!target.equals(list.target)) {
                                return DataResult.error(() -> "Cannot encode simple action list with target: " + list.target);
                            }
                            return DataResult.success(list.behaviors);
                        }
                );
        return Codec.either(simpleCodec, mapCodec(type, target).codec())
                .xmap(either -> either.map(Function.identity(), Function.identity()), Either::right);
    }

    private final List<IGameBehavior> behaviors;
    public final ActionTarget<T> target;

    private final GameEventListeners listeners = new GameEventListeners();

    private boolean registered;

    public GameActionList(List<IGameBehavior> behaviors, ActionTarget<T> target) {
        this.behaviors = behaviors;
        this.target = target;
    }

    public void register(IGamePhase game, EventRegistrar events) {
        if (registered) {
            throw new IllegalStateException("GameActionList has already been registered");
        }
        for (IGameBehavior behavior : behaviors) {
            behavior.register(game, events.redirect(GameActionEvents::matches, listeners));
        }
        registered = true;
    }

    public <T1> boolean applyIf(Supplier<Codec<? extends ActionTarget<T1>>> type, IGamePhase phase, GameActionContext context, Iterable<T1> sources) {
        if (type.get() == target.type()) {
            return apply(phase, context, (Iterable<T>) sources);
        }
        return false;
    }

    public boolean apply(IGamePhase phase, GameActionContext context) {
        return apply(phase, context, target.resolve(phase, List.of()));
    }

    public boolean apply(IGamePhase phase, GameActionContext context, T... sources) {
        return apply(phase, context, Arrays.asList(sources));
    }

    public boolean apply(IGamePhase phase, GameActionContext context, Iterable<T> sources) {
        if (!registered) {
            throw new IllegalStateException("Cannot dispatch action, GameActionList has not been registered");
        }
        return listeners.invoker(GameActionEvents.APPLY).apply(context) | target.apply(phase, listeners, context, sources);
    }

}
