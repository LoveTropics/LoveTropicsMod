package com.lovetropics.minigames.common.content.qottott;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.util.TranslationCollector;

public class QottottTexts {
	public static final TranslationCollector KEYS = new TranslationCollector(Constants.MODID + ".minigame.qottott.");

	static {
		KEYS.add("sidebar.title", "Qottot");
		KEYS.add("instruction", "Pick up coins!");
		KEYS.add("sidebar.top_players", "Top Players:");
		KEYS.add("waiting", "Pick a kit before the game starts!");
		KEYS.add("intro", "Pick a kit! The game will begin soon!");
		KEYS.add("started", "The game has begun - walk into the portal to enter the map!");
		KEYS.add("kit.picked", "You have selected: %s");
		KEYS.add("kit.acrobat", "Acrobat");
		KEYS.add("kit.assassin", "Assassin");
		KEYS.add("kit.glass_cannon", "Glass Cannon");
		KEYS.add("power_up.generic", "2x coins for %seconds% seconds");
		KEYS.add("power_up.speed", "2x coins & increased Speed for %seconds% seconds");
		KEYS.add("power_up.knockback", "2x coins & increased Knockback for %seconds% seconds");
		KEYS.add("power_up.knockback_resistance", "2x coins & Knockback Resistance for %seconds% seconds");
		KEYS.add("spawn.power_up", "%item% has spawned!");
	}
}
