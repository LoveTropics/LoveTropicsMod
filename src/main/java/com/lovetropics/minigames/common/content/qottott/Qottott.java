package com.lovetropics.minigames.common.content.qottott;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.qottott.behavior.ItemDropperBehavior;
import com.lovetropics.minigames.common.content.qottott.behavior.ItemPickupPointsBehavior;
import com.lovetropics.minigames.common.content.qottott.behavior.LobbyWithPortalBehavior;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public class Qottott {
	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final GameBehaviorEntry<ItemDropperBehavior> ITEM_DROPPER = REGISTRATE.object("item_dropper").behavior(ItemDropperBehavior.CODEC).register();
	public static final GameBehaviorEntry<ItemPickupPointsBehavior> ITEM_PICKUP_POINTS = REGISTRATE.object("item_pickup_points").behavior(ItemPickupPointsBehavior.CODEC).register();
	public static final GameBehaviorEntry<LobbyWithPortalBehavior> LOBBY_WITH_PORTAL = REGISTRATE.object("lobby_with_portal").behavior(LobbyWithPortalBehavior.CODEC).register();

	public static void init() {
	}
}
