package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.util.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.util.text.ITextComponent;

public class ScheduledMessagesBehavior implements IGameBehavior {
	public static final Codec<ScheduledMessagesBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.long2Object(MoreCodecs.TEXT).fieldOf("messages").forGetter(c -> c.scheduledMessages)
		).apply(instance, ScheduledMessagesBehavior::new);
	});

	private final Long2ObjectMap<ITextComponent> scheduledMessages;

	public ScheduledMessagesBehavior(final Long2ObjectMap<ITextComponent> scheduledMessages) {
		this.scheduledMessages = scheduledMessages;
	}

	@Override
	public void register(IGameInstance registerGame, GameEventListeners events) {
		events.listen(GameLifecycleEvents.TICK, game -> {
			ITextComponent message = scheduledMessages.remove(game.ticks());
			if (message != null) {
				game.getPlayers().sendMessage(message);
			}
		});
	}
}
