package com.lovetropics.minigames.common.content.trash_dive;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.util.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.LoveTropicsRegistrate;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class TrashDive {
	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final GameBehaviorEntry<PlaceTrashBehavior> PLACE_TRASH = REGISTRATE.object("place_trash")
			.behavior(PlaceTrashBehavior.CODEC)
			.register();
	public static final GameBehaviorEntry<TrashCollectionBehavior> TRASH_COLLECTION = REGISTRATE.object("trash_collection")
			.behavior(TrashCollectionBehavior.CODEC)
			.register();

	public static void init() {
	}

	@SubscribeEvent
	public static void onRegisterCommands(final RegisterCommandsEvent event) {
		CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
		ScanAreaCommand.register(dispatcher);
	}
}
