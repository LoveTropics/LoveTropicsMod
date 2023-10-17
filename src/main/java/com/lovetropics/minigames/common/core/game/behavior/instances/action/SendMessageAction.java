package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SendMessageAction(TemplatedText message) implements IGameBehavior {
	public static final MapCodec<SendMessageAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			TemplatedText.CODEC.fieldOf("message").forGetter(SendMessageAction::message)
	).apply(i, SendMessageAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target) -> {
			target.sendSystemMessage(message.apply(context), false);
			return true;
		});
	}
}
