package com.lovetropics.minigames.common.content.qottott;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.qottott.behavior.ItemDropperBehavior;
import com.lovetropics.minigames.common.content.qottott.behavior.GivePointsAction;
import com.lovetropics.minigames.common.content.qottott.behavior.KillBonusBehavior;
import com.lovetropics.minigames.common.content.qottott.behavior.KitSelectionBehavior;
import com.lovetropics.minigames.common.content.qottott.behavior.LobbyWithPortalBehavior;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public class Qottott {
	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final GameBehaviorEntry<ItemDropperBehavior> ITEM_DROPPER = REGISTRATE.object("item_dropper").behavior(ItemDropperBehavior.CODEC).register();
	public static final GameBehaviorEntry<GivePointsAction> GIVE_POINTS = REGISTRATE.object("give_points").behavior(GivePointsAction.CODEC).register();
	public static final GameBehaviorEntry<LobbyWithPortalBehavior> LOBBY_WITH_PORTAL = REGISTRATE.object("lobby_with_portal").behavior(LobbyWithPortalBehavior.CODEC).register();
	public static final GameBehaviorEntry<KitSelectionBehavior> KIT_SELECTION = REGISTRATE.object("kit_selection").behavior(KitSelectionBehavior.CODEC).register();
	public static final GameBehaviorEntry<KillBonusBehavior> KILL_BONUS = REGISTRATE.object("kill_bonus").behavior(KillBonusBehavior.CODEC).register();

	public static void init() {
	}
}
