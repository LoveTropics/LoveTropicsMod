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

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class GameActionList<T, A extends ActionTarget<T>> {
    public static final GameActionList<ServerPlayer, PlayerActionTarget> EMPTY = new GameActionList<>(List.of(), PlayerActionTarget.SOURCE);

    public static final MapCodec<GameActionList<?, ?>> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            MoreCodecs.listOrUnit(IGameBehavior.CODEC).fieldOf("actions").forGetter(list -> list.behaviors),
            ActionTarget.FALLBACK_PLAYER.optionalFieldOf("target", PlayerActionTarget.SOURCE).forGetter(list -> list.target)
    ).apply(i, GameActionList::new));

    private static final Codec<GameActionList<?, ?>> SIMPLE_CODEC = MoreCodecs.listOrUnit(IGameBehavior.CODEC)
            .flatComapMap(
                    behaviors -> new GameActionList<>(behaviors, PlayerActionTarget.SOURCE),
                    list -> {
                        if (!(list.target instanceof PlayerActionTarget tg) || tg.target() != PlayerActionTarget.Target.SOURCE) {
                            return DataResult.error(() -> "Cannot encode simple action list with target: " + list.target);
                        }
                        return DataResult.success(list.behaviors);
                    }
            );

    public static final Codec<GameActionList> CODEC = Codec.either(SIMPLE_CODEC, MAP_CODEC.codec())
            .xmap(either -> either.map(Function.identity(), Function.identity()), Either::right);
    public static final Codec<GameActionList<?, ?>> TYPE_SAFE_CODEC = (Codec<GameActionList<?,?>>) (Object) CODEC;

    public static <T, A extends ActionTarget<T>> Codec<GameActionList<T, A>> mandateType(Supplier<Codec<A>> type) {
        final Function<GameActionList<?, ?>, DataResult<GameActionList<T, A>>> function = gameActionList -> {
            if (gameActionList.target.type() != type.get()) {
                return DataResult.error(() -> "Action list target must be of type: " + ActionTargetTypes.REGISTRY.get().getKey(type.get()));
            }
            return DataResult.success((GameActionList<T, A>) gameActionList);
        };
        return TYPE_SAFE_CODEC.flatXmap(function, function);
    }

    private final List<IGameBehavior> behaviors;
    public final A target;

    private final GameEventListeners listeners = new GameEventListeners();

    public GameActionList(List<IGameBehavior> behaviors, A target) {
        this.behaviors = behaviors;
        this.target = target;
    }

    public void register(IGamePhase game, EventRegistrar events) {
        for (IGameBehavior behavior : behaviors) {
            behavior.register(game, events.redirect(GameActionEvents::matches, listeners));
        }
    }

    public boolean applyPlayer(IGamePhase game, GameActionContext context, ServerPlayer... sources) {
        return applyPlayer(game, context, Arrays.asList(sources));
    }

    public boolean applyPlayer(IGamePhase game, GameActionContext context, Iterable<ServerPlayer> sources) {
        return this.applyIf(ActionTargetTypes.PLAYER, game, context, sources);
    }

    public <T1, A1 extends ActionTarget<T1>> boolean applyIf(Supplier<Codec<A1>> type, IGamePhase phase, GameActionContext context, Iterable<T1> sources) {
        if (type.get() == target.type()) {
            return apply(phase, context, (Iterable) sources);
        }
        return false;
    }

    public boolean apply(IGamePhase phase, GameActionContext context) {
        return apply(phase, context, target.resolve(phase, List.of()));
    }

    public boolean apply(IGamePhase phase, GameActionContext context, Iterable<T> sources) {
        return listeners.invoker(GameActionEvents.APPLY).apply(context) | target.apply(phase, listeners, context, sources);
    }

}
