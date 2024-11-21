package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.ActionTarget;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.action.NoneActionTarget;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;

public record DelayedAction<T>(
		IntProvider delay,
		GameActionList<?> actions,
		ActionTarget<T> target
) implements IGameBehavior {
	public static final MapCodec<DelayedAction<?>> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			IntProvider.POSITIVE_CODEC.fieldOf("delay").forGetter(b -> b.delay),
			GameActionList.MAP_CODEC.forGetter(c -> c.actions),
			ActionTarget.CODEC.optionalFieldOf("target", NoneActionTarget.INSTANCE).forGetter(c -> c.target)
	).apply(i, DelayedAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		actions.register(game, events);

		target.listenAndCaptureSource(events, (context, targets) -> {
			int ticks = delay.sample(game.random());
			game.scheduler().runAfterTicks(ticks, () -> {
				if (actions.target.type() == target.type()) {
					actions.applyIf(target.type(), game, context, targets);
				} else {
					actions.apply(game, context);
				}
			});
			return true;
		});
	}
}
