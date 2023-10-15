package com.lovetropics.minigames.common.core.game.behavior.action;

import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.function.Supplier;

public record ApplyToBehavior<T, A extends ActionTarget<T>>(A target, GameActionList<T> actions, Supplier<GameBehaviorType<ApplyToBehavior<T, A>>> type) implements IGameBehavior {
    public static final Codec<ApplyToBehavior<Plot, PlotActionTarget>> PLOT_CODEC = RecordCodecBuilder.create(in -> in.group(
            PlotActionTarget.Target.CODEC.optionalFieldOf("target", PlotActionTarget.Target.ALL).xmap(PlotActionTarget::new, PlotActionTarget::target).forGetter(ApplyToBehavior::target),
            GameActionList.codec(ActionTargetTypes.PLOT, new PlotActionTarget(PlotActionTarget.Target.SOURCE)).fieldOf("actions").forGetter(ApplyToBehavior::actions)
    ).apply(in, (a, b) -> new ApplyToBehavior<>(a, b, GameBehaviorTypes.APPLY_TO_PLOT)));
    public static final Codec<ApplyToBehavior<ServerPlayer, PlayerActionTarget>> PLAYER_CODEC = RecordCodecBuilder.create(in -> in.group(
            PlayerActionTarget.Target.CODEC.optionalFieldOf("target", PlayerActionTarget.Target.ALL).xmap(PlayerActionTarget::new, PlayerActionTarget::target).forGetter(ApplyToBehavior::target),
            GameActionList.codec(ActionTargetTypes.PLAYER, PlayerActionTarget.SOURCE).fieldOf("actions").forGetter(ApplyToBehavior::actions)
    ).apply(in, (a, b) -> new ApplyToBehavior<>(a, b, GameBehaviorTypes.APPLY_TO_PLAYER)));

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        actions.register(game, events);
        events.listen(GameActionEvents.APPLY, context -> actions.apply(game, context, target.resolve(game, List.of())));
    }

    @Override
    public Supplier<GameBehaviorType<?>> behaviorType() {
        return type::get;
    }
}
