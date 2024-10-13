package com.lovetropics.minigames.common.content.crafting_bee;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;

public class CraftingBee {
    private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

    public static final GameBehaviorEntry<CraftingBeeBehavior> CRAFTING_BEE = REGISTRATE.object("crafting_bee")
            .behavior(CraftingBeeBehavior.CODEC)
            .register();

    public static void init() {
    }
}
