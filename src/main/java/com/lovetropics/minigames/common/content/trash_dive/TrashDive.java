package com.lovetropics.minigames.common.content.trash_dive;

import com.lovetropics.minigames.Constants;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class TrashDive {
	@SubscribeEvent
	public static void onServerAboutToStart(final FMLServerAboutToStartEvent event) {
		CommandDispatcher<CommandSource> dispatcher = event.getServer().getCommandManager().getDispatcher();
		ScanAreaCommand.register(dispatcher);
	}
}
