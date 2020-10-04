package com.lovetropics.minigames.common.item;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.item.map.EditRegionItem;
import com.lovetropics.minigames.common.item.minigame.AcidRepellentUmbrellaItem;
import com.lovetropics.minigames.common.item.minigame.SuperSunscreenItem;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.ItemEntry;

public class MinigameItems {
    
    private static final Registrate REGISTRATE = LoveTropics.registrate();
 
    public static final ItemEntry<SuperSunscreenItem> SUPER_SUNSCREEN = REGISTRATE.item("super_sunscreen", SuperSunscreenItem::new)
            .register();
    
    public static final ItemEntry<AcidRepellentUmbrellaItem> ACID_REPELLENT_UMBRELLA = REGISTRATE.item("acid_repellent_umbrella", AcidRepellentUmbrellaItem::new)
            .register();

    public static final ItemEntry<EditRegionItem> EDIT_REGION = REGISTRATE.item("edit_region", EditRegionItem::new)
            .register();
    
    public static void init() {}
}
