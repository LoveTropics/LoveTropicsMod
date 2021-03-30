package com.lovetropics.minigames.common.content.survive_the_tide;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.render.entity.DriftwoodRenderer;
import com.lovetropics.minigames.common.content.survive_the_tide.entity.DriftwoodEntity;
import com.lovetropics.minigames.common.content.survive_the_tide.item.AcidRepellentUmbrellaItem;
import com.lovetropics.minigames.common.content.survive_the_tide.item.PaddleItem;
import com.lovetropics.minigames.common.content.survive_the_tide.item.SuperSunscreenItem;
import com.mojang.brigadier.CommandDispatcher;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class SurviveTheTide {
	private static final Registrate REGISTRATE = LoveTropics.registrate();

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

	public static void init() {}

	@SubscribeEvent
	public static void onServerAboutToStart(final FMLServerAboutToStartEvent event) {
		CommandDispatcher<CommandSource> dispatcher = event.getServer().getCommandManager().getDispatcher();
		ResetIslandChestsCommand.register(dispatcher);
	}
}
