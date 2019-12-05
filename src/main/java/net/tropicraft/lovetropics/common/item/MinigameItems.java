package net.tropicraft.lovetropics.common.item;

import com.tterrag.registrate.Registrate;

import net.minecraftforge.fml.RegistryObject;
import net.tropicraft.lovetropics.LoveTropics;
import net.tropicraft.lovetropics.common.block.LoveTropicsBlocks;
import net.tropicraft.lovetropics.common.item.minigame.AcidRepellentUmbrellaItem;
import net.tropicraft.lovetropics.common.item.minigame.SuperSunscreenItem;

public class MinigameItems {
    
    private static final Registrate REGISTRATE = LoveTropicsBlocks.REGISTRATE;
 
    public static final RegistryObject<SuperSunscreenItem> SUPER_SUNSCREEN = REGISTRATE.item("super_sunscreen", SuperSunscreenItem::new)
            .properties(p -> p.group(LoveTropics.LOVE_TROPICS_ITEM_GROUP))
            .register();
    
    public static final RegistryObject<AcidRepellentUmbrellaItem> ACID_REPELLENT_UMBRELLA = REGISTRATE.item("acid_repellent_umbrella", AcidRepellentUmbrellaItem::new)
            .properties(p -> p.group(LoveTropics.LOVE_TROPICS_ITEM_GROUP))
            .register();
    
    public static void init() {}
}
