package com.lovetropics.minigames.common.core.game.behavior.action;

import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record ApplyToBehavior<T, A extends ActionTarget<T>>(A target, GameActionList<T> actions) implements IGameBehavior {
    public static final Codec<ApplyToBehavior<Plot, PlotActionTarget>> PLOT_CODEC = RecordCodecBuilder.create(in -> in.group(
            PlotActionTarget.Target.CODEC.optionalFieldOf("target", PlotActionTarget.Target.ALL).xmap(PlotActionTarget::new, PlotActionTarget::target).forGetter(ApplyToBehavior::target),
            GameActionList.mandateTypeDefaultingTarget(ActionTargetTypes.PLOT, new PlotActionTarget(PlotActionTarget.Target.SOURCE)).fieldOf("actions").forGetter(ApplyToBehavior::actions)
    ).apply(in, ApplyToBehavior::new));
    public static final Codec<ApplyToBehavior<ServerPlayer, PlayerActionTarget>> PLAYER_CODEC = RecordCodecBuilder.create(in -> in.group(
            PlayerActionTarget.Target.CODEC.optionalFieldOf("target", PlayerActionTarget.Target.ALL).xmap(PlayerActionTarget::new, PlayerActionTarget::target).forGetter(ApplyToBehavior::target),
            GameActionList.mandateTypeDefaultingTarget(ActionTargetTypes.PLAYER, PlayerActionTarget.SOURCE).fieldOf("actions").forGetter(ApplyToBehavior::actions)
    ).apply(in, ApplyToBehavior::new));

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        actions.register(game, events);
        events.listen(GameActionEvents.APPLY, context -> actions.apply(game, context, target.resolve(game, List.of())));
    }
}
