package com.lovetropics.minigames.common.content.qottott;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.util.TranslationCollector;

public class QottottTexts {
	public static final TranslationCollector KEYS = new TranslationCollector(Constants.MODID + ".minigame.qottott.");

	static {
		KEYS.add("sidebar.title", "Qottot");
		KEYS.add("sidebar.instruction", "Pick up points!");
		KEYS.add("sidebar.top_players", "Top Players:");
		KEYS.add("time_remaining", "Time Remaining: %time%...");
		KEYS.add("kit.picked", "You have selected: %s");
		KEYS.add("kit.acrobat", "Acrobat");
		KEYS.add("kit.assassin", "Assassin");
		KEYS.add("kit.glass_cannon", "Glass Cannon");
		KEYS.add("power_up.generic", "2x points for %seconds% seconds");
		KEYS.add("power_up.speed", "2x points & increased Speed for %seconds% seconds");
		KEYS.add("power_up.knockback", "2x points & increased Knockback for %seconds% seconds");
		KEYS.add("power_up.knockback_resistance", "2x points & Knockback Resistance for %seconds% seconds");
		KEYS.add("spawn.power_up", "%item% has spawned!");
		KEYS.add("kill_bonus.tag", "KILL BONUS: %s");
		KEYS.add("kill_bonus.announce", "%target% has a %count% point kill bonus!");
	}
}
