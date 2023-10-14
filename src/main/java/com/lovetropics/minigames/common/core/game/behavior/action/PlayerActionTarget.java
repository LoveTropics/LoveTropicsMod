package com.lovetropics.minigames.common.core.game.behavior.action;

import com.google.common.collect.Lists;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.function.ToBooleanBiFunction;

import java.util.List;

public record PlayerActionTarget(Target target) implements ActionTarget<ServerPlayer> {
    public static final PlayerActionTarget SOURCE = new PlayerActionTarget(Target.SOURCE);
    public static final Codec<PlayerActionTarget> CODEC = RecordCodecBuilder.create(in -> in.group(
            Target.CODEC.optionalFieldOf("target", PlayerActionTarget.Target.SOURCE).forGetter(PlayerActionTarget::target)
    ).apply(in, PlayerActionTarget::new));

    @Override
    public List<ServerPlayer> resolve(IGamePhase phase, Iterable<ServerPlayer> sources) {
        return target.resolve(phase, sources);
    }

    @Override
    public boolean apply(IGamePhase game, GameEventListeners listeners, GameActionContext actionContext, Iterable<ServerPlayer> sources) {
        boolean result = false;
        for (ServerPlayer target : target.resolve(game, sources)) {
            result |= listeners.invoker(GameActionEvents.APPLY_TO_PLAYER).apply(actionContext, target);
        }
        return result;
    }

    @Override
    public void listenAndCaptureSource(EventRegistrar listeners, ToBooleanBiFunction<GameActionContext, Iterable<ServerPlayer>> listener) {
        listeners.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target1) -> listener.applyAsBoolean(context, List.of(target1)));
    }

    @Override
    public Codec<? extends ActionTarget<ServerPlayer>> type() {
        return ActionTargetTypes.PLAYER.get();
    }

    public enum Target implements StringRepresentable {
        NONE("none"),
        SOURCE("source"),
        PARTICIPANTS("participants"),
        SPECTATORS("spectators"),
        ALL("all"),
        ;

        public static final Codec<PlayerActionTarget.Target> CODEC = MoreCodecs.stringVariants(values(), PlayerActionTarget.Target::getSerializedName);

        private final String name;

        Target(String name) {
            this.name = name;
        }

        public List<ServerPlayer> resolve(IGamePhase game, Iterable<ServerPlayer> sources) {
            // Copy the lists because we might otherwise get concurrent modification from whatever the actions do!
            return switch (this) {
                case NONE -> List.of();
                case SOURCE -> Lists.newArrayList(sources);
                case PARTICIPANTS -> Lists.newArrayList(game.getParticipants());
                case SPECTATORS -> Lists.newArrayList(game.getSpectators());
                case ALL -> Lists.newArrayList(game.getAllPlayers());
            };
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
