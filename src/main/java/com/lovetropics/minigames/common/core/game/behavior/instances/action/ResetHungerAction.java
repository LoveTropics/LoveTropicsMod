package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.MapCodec;

public record ResetHungerAction() implements IGameBehavior {
    public static final MapCodec<ResetHungerAction> CODEC = MapCodec.unit(ResetHungerAction::new);
    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target) -> {
            target.getFoodData().setFoodLevel(20);
            target.getFoodData().setSaturation(5);
            target.getFoodData().setExhaustion(0);
            return true;
        });
    }
}
