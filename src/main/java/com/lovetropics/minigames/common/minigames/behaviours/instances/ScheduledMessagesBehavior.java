package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.Map;

public class ScheduledMessagesBehavior implements IMinigameBehavior
{
	private final Map<Long, ITextComponent> scheduledMessages;

	public ScheduledMessagesBehavior(final Map<Long, ITextComponent> scheduledMessages) {
		this.scheduledMessages = scheduledMessages;
	}

	public static <T> ScheduledMessagesBehavior parse(Dynamic<T> root) {
		final Map<Long, ITextComponent> messages = root.get("messages").asMap(
				key -> Long.parseLong(key.asString("0")),
				Util::getText
		);

		return new ScheduledMessagesBehavior(messages);
	}

	@Override
	public void worldUpdate(final IMinigameInstance minigame, World world) {
		if (scheduledMessages.containsKey(minigame.ticks())) {
			final ITextComponent message = scheduledMessages.get(minigame.ticks());

			minigame.getPlayers().sendMessage(message);
		}
	}
}
