package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.MoreCodecs;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;

public class ScheduledMessagesBehavior implements IMinigameBehavior {
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
	public void worldUpdate(final IMinigameInstance minigame, ServerWorld world) {
		if (scheduledMessages.containsKey(minigame.ticks())) {
			final ITextComponent message = scheduledMessages.get(minigame.ticks());

			minigame.getPlayers().sendMessage(message);
		}
	}
}
