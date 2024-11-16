package com.lovetropics.minigames.common.content.pillars_of_wealth;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;

public class PillarsOfWealth {
    private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

    public static final GameBehaviorEntry<PillarsOfWealthBehaviour> BEHAVIOR = REGISTRATE.object("pillars_of_wealth")
            .behavior(PillarsOfWealthBehaviour.CODEC)
            .register();

    public static void init() {
    }
}
