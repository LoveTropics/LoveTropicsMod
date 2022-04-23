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

public class ScheduledMessagesBehavior implements IGameBehavior {
	public static final Codec<ScheduledMessagesBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.long2Object(MoreCodecs.TEXT).fieldOf("messages").forGetter(c -> c.scheduledMessages)
		).apply(instance, ScheduledMessagesBehavior::new);
	});

	private final Long2ObjectMap<Component> scheduledMessages;

	public ScheduledMessagesBehavior(final Long2ObjectMap<Component> scheduledMessages) {
		this.scheduledMessages = scheduledMessages;
	}

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
