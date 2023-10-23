package com.lovetropics.minigames.common.content.trash_dive;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.util.TranslationCollector;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class TrashDiveTexts {
	public static final TranslationCollector KEYS = new TranslationCollector(Constants.MODID + ".minigame.trash_dive.");

	public static final Component SIDEBAR_TITLE = KEYS.add("sidebar.title", "Trash Dive").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD);
	public static final Component SIDEBAR_INSTRUCTION = KEYS.add("sidebar.instruction", "Pick up trash!").withStyle(ChatFormatting.GREEN);
	public static final TranslationCollector.Fun1 SIDEBAR_COLLECTED = KEYS.add1("sidebar.collected", "%s collected").withStyle(ChatFormatting.GRAY);
	public static final Component SIDEBAR_TOP_PLAYERS = KEYS.add("sidebar.top_players", "MVPs:").withStyle(ChatFormatting.GREEN);

	static {
		KEYS.add("time_remaining", "Time Remaining: %time%...");
	}
}
