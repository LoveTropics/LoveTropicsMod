package com.lovetropics.minigames.common.content.conservation_exploration;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.trash_dive.ScanAreaCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.command.CommandSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class ConservationExploration {
	private static final Registrate REGISTRATE = LoveTropics.registrate();

	public static final ItemEntry<RecordCreatureItem> RECORD_CREATURE = REGISTRATE.item("record_creature", RecordCreatureItem::new)
			.register();

	public static void init() {}

	@SubscribeEvent
	private void onServerAboutToStart(final FMLServerAboutToStartEvent event) {
		CommandDispatcher<CommandSource> dispatcher = event.getServer().getCommandManager().getDispatcher();
		ScanAreaCommand.register(dispatcher);
	}
}
