package com.lovetropics.minigames.common.content.turtle_race;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.render.entity.DriftwoodRenderer;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.RevealPlayersBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.RisingTidesGameBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.SttChatBroadcastBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.SttPetsBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.SttSidebarBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.SttWinLogicBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.SurviveTheTideRulesetBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.SurviveTheTideWeatherControlBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.WorldBorderGameBehavior;
import com.lovetropics.minigames.common.content.survive_the_tide.entity.DriftwoodEntity;
import com.lovetropics.minigames.common.content.survive_the_tide.item.AcidRepellentUmbrellaItem;
import com.lovetropics.minigames.common.content.survive_the_tide.item.PaddleItem;
import com.lovetropics.minigames.common.content.survive_the_tide.item.SuperSunscreenItem;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class TurtleRace {
	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final GameBehaviorEntry<TurtleRiderBehavior> TURTLE_RIDER = REGISTRATE.object("turtle_rider")
			.behavior(TurtleRiderBehavior.CODEC)
			.register();

	public static final GameBehaviorEntry<RaceTrackBehavior> RACE_TRACK = REGISTRATE.object("race_track")
			.behavior(RaceTrackBehavior.CODEC)
			.register();

	public static void init() {
	}
}
