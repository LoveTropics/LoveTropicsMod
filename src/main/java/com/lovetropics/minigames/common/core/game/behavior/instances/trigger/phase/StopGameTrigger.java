package com.lovetropics.minigames.common.core.game.behavior.instances.trigger.phase;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record StopGameTrigger(GameActionList<Void> actions, Optional<GameActionList<Void>> finish, Optional<GameActionList<Void>> cancel) implements IGameBehavior {
    public static final Codec<StopGameTrigger> CODEC = RecordCodecBuilder.create(in -> in.group(
            GameActionList.VOID.fieldOf("actions").forGetter(StopGameTrigger::actions),
            GameActionList.VOID.optionalFieldOf("finish").forGetter(StopGameTrigger::finish),
            GameActionList.VOID.optionalFieldOf("cancel").forGetter(StopGameTrigger::cancel)
    ).apply(in, StopGameTrigger::new));

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(GamePhaseEvents.STOP, reason -> {
            actions.apply(game, GameActionContext.EMPTY);
            if (reason.isFinished()) {
                finish.ifPresent(f -> f.apply(game, GameActionContext.EMPTY));
            } else {
                cancel.ifPresent(c -> c.apply(game, GameActionContext.EMPTY));
            }
        });
    }
}
