package com.lovetropics.minigames.common.content.paint_party;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;

public class PaintParty {
    private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

    public static final GameBehaviorEntry<PaintPartyBehaviour> BEHAVIOR = REGISTRATE.object("paint_party")
            .behavior(PaintPartyBehaviour.CODEC)
            .register();

    public static void init() {
    }
}
