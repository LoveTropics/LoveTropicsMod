package net.tropicraft.lovetropics.common.item;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.RegistryEntry;

import net.tropicraft.lovetropics.LoveTropics;
import net.tropicraft.lovetropics.common.item.minigame.AcidRepellentUmbrellaItem;
import net.tropicraft.lovetropics.common.item.minigame.SuperSunscreenItem;

public class MinigameItems {
    
    private static final Registrate REGISTRATE = LoveTropics.registrate();
 
    public static final RegistryEntry<SuperSunscreenItem> SUPER_SUNSCREEN = REGISTRATE.item("super_sunscreen", SuperSunscreenItem::new)
            .register();
    
    public static final RegistryEntry<AcidRepellentUmbrellaItem> ACID_REPELLENT_UMBRELLA = REGISTRATE.item("acid_repellent_umbrella", AcidRepellentUmbrellaItem::new)
            .register();
    
    public static void init() {}
}
