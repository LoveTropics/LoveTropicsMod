package com.lovetropics.minigames.common.content.survive_the_tide;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.render.entity.DriftwoodRenderer;
import com.lovetropics.minigames.common.content.survive_the_tide.behavior.*;
import com.lovetropics.minigames.common.content.survive_the_tide.entity.DriftwoodEntity;
import com.lovetropics.minigames.common.content.survive_the_tide.item.AcidRepellentUmbrellaItem;
import com.lovetropics.minigames.common.content.survive_the_tide.item.PaddleItem;
import com.lovetropics.minigames.common.content.survive_the_tide.item.SuperSunscreenItem;
import com.lovetropics.minigames.common.util.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.LoveTropicsRegistrate;
import com.mojang.brigadier.CommandDispatcher;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class SurviveTheTide {
	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final ItemEntry<SuperSunscreenItem> SUPER_SUNSCREEN = REGISTRATE.item("super_sunscreen", SuperSunscreenItem::new)
			.register();

	public static final ItemEntry<AcidRepellentUmbrellaItem> ACID_REPELLENT_UMBRELLA = REGISTRATE.item("acid_repellent_umbrella", AcidRepellentUmbrellaItem::new)
			.register();

	public static final ItemEntry<PaddleItem> PADDLE = REGISTRATE.item("paddle", PaddleItem::new)
			.register();

	public static final RegistryEntry<EntityType<DriftwoodEntity>> DRIFTWOOD = REGISTRATE.entity("driftwood", DriftwoodEntity::new, EntityClassification.MISC)
			.properties(properties -> properties.size(2.0F, 1.0F).setShouldReceiveVelocityUpdates(true).setUpdateInterval(3))
			.defaultLang()
			.renderer(() -> DriftwoodRenderer::new)
			.register();

	public static final GameBehaviorEntry<RisingTidesGameBehavior> RISING_TIDES = REGISTRATE.object("rising_tides")
			.behavior(RisingTidesGameBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<SurviveTheTideRulesetBehavior> SURVIVE_THE_TIDE_RULESET = REGISTRATE.object("survive_the_tide_ruleset")
			.behavior(SurviveTheTideRulesetBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<SttChatBroadcastBehavior> SURVIVE_THE_TIDE_CHAT_BROADCAST = REGISTRATE.object("survive_the_tide_chat_broadcast")
			.behavior(SttChatBroadcastBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<SttWinLogicBehavior> SURVIVE_THE_TIDE_WIN_LOGIC = REGISTRATE.object("survive_the_tide_win_logic")
			.behavior(SttWinLogicBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<RevealPlayersBehavior> REVEAL_PLAYERS = REGISTRATE.object("reveal_players")
			.behavior(RevealPlayersBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<WorldBorderGameBehavior> WORLD_BORDER = REGISTRATE.object("world_border")
			.behavior(WorldBorderGameBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<SurviveTheTideWeatherBehavior> WEATHER_EVENTS = REGISTRATE.object("weather_events")
			.behavior(SurviveTheTideWeatherBehavior.CODEC)
			.register();

	public static void init() {
	}

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
		ResetIslandChestsCommand.register(dispatcher);
	}
}
