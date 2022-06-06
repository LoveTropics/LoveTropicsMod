package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.network.chat.Component;

public record ScheduledMessagesBehavior(Long2ObjectMap<Component> scheduledMessages) implements IGameBehavior {
	public static final Codec<ScheduledMessagesBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.long2Object(MoreCodecs.TEXT).fieldOf("messages").forGetter(c -> c.scheduledMessages)
	).apply(i, ScheduledMessagesBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePhaseEvents.TICK, () -> {
			Component message = scheduledMessages.remove(game.ticks());
			if (message != null) {
				game.getAllPlayers().sendMessage(message);
			}
		});
	}
}
