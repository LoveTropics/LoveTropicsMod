package com.lovetropics.minigames.common.content.turtle_race;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.util.TranslationCollector;
import com.lovetropics.minigames.common.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TurtleRaceTexts {
	public static final TranslationCollector KEYS = new TranslationCollector(Constants.MODID + ".minigame.turtle_race.");

	public static final Component CHECKMARK = Component.literal("\u2714").withStyle(ChatFormatting.GREEN);

	public static final Component WARNING = KEYS.add("warning", "Warning!").withStyle(ChatFormatting.RED);
	public static final Component WRONG_WAY = KEYS.add("wrong_way", "You are going the wrong way!").withStyle(ChatFormatting.GOLD);
	public static final Component UNKNOWN_PLAYER = KEYS.add("unknown_player", "Unknown");
	public static final TranslationCollector.Fun2 LAP_COUNT = KEYS.add2("lap_count", "Lap #%s of %s").withStyle(ChatFormatting.AQUA);
	private static final TranslationCollector.Fun2 LAP_PROGRESS = KEYS.add2("lap_progress", "(Lap #%s; %s%%)").withStyle(ChatFormatting.GRAY);
	private static final TranslationCollector.Fun1 PROGRESS = KEYS.add1("progress", "(%s%%)").withStyle(ChatFormatting.GRAY);
	private static final TranslationCollector.Fun3 FINISHED_LAP = KEYS.add3("finished_lap", "%s finished lap #%s in %s!").withStyle(ChatFormatting.AQUA);
	private static final TranslationCollector.Fun2 FINISHED = KEYS.add2("finished", "%s finished in %s!").withStyle(ChatFormatting.AQUA);
	private static final TranslationCollector.Fun1 CLOSING = KEYS.add1("closing", "The game will finish in %s seconds!").withStyle(ChatFormatting.AQUA);

	static {
		KEYS.add("go", "GO!");
		KEYS.add("speed_boost", "Speed boost! You passed a checkpoint!");

		KEYS.add("donation.speed_boost_1_package", "%sender% sent you a SPEED BOOST for 1 second! Wow");
		KEYS.add("donation.speed_boost_5_package", "%sender% sent you a SPEED BOOST for 5 seconds!");
		KEYS.add("donation.speed_boost_30_package", "%sender% sent you a SPEED BOOST for 30 seconds!");
		KEYS.add("donation.slowness_1_package", "%sender% sent you a SLOWNESS PACKAGE for 1 second! Wow");
		KEYS.add("donation.slowness_5_package", "%sender% sent you a SLOWNESS PACKAGE for 5 seconds!");
		KEYS.add("donation.slowness_30_package", "%sender% sent you a SLOWNESS PACKAGE for 30 seconds!");
	}

	private static MutableComponent formatTime(long totalSeconds) {
		return Component.literal(Util.formatMinutesSeconds(totalSeconds)).withStyle(ChatFormatting.GOLD);
	}

	public static Component progress(float progress) {
		return PROGRESS.apply(Math.round(progress * 100.0f));
	}

	public static Component lapProgress(int lap, float progress) {
		return LAP_PROGRESS.apply(lap + 1, Math.round(progress * 100.0f));
	}

	private static MutableComponent withCheckmark(Component text) {
		return Component.empty().append(CHECKMARK).append(" ").append(text);
	}

	public static Component finishedLap(Component playerName, int lap, long totalSeconds) {
		return withCheckmark(FINISHED_LAP.apply(playerName, lap + 1, formatTime(totalSeconds)));
	}

	public static Component finished(Component playerName, long totalSeconds) {
		return withCheckmark(FINISHED.apply(playerName, formatTime(totalSeconds)));
	}

	public static Component closing(int seconds) {
		return CLOSING.apply(Component.literal(String.valueOf(seconds)).withStyle(ChatFormatting.GOLD));
	}
}
