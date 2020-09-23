package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.Map;

public class ScheduledMessagesBehavior implements IMinigameBehavior
{
	private final Map<Long, String> scheduledMessages;

	public ScheduledMessagesBehavior(final Map<Long, String> scheduledMessages) {
		this.scheduledMessages = scheduledMessages;
	}

	public static <T> ScheduledMessagesBehavior parse(Dynamic<T> root) {
		final Map<Long, String> messages = root.get("messages").asMap(
				key -> Long.parseLong(key.asString("0")),
				value -> value.asString("")
		);

		return new ScheduledMessagesBehavior(messages);
	}

	@Override
	public void worldUpdate(final IMinigameInstance minigame, World world) {
		if (scheduledMessages.containsKey(minigame.ticks())) {
			final String message = scheduledMessages.get(minigame.ticks());

			minigame.getPlayers().sendMessage(new TranslationTextComponent(message));
		}
	}
}
