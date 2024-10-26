package com.lovetropics.minigames.common.content.connect4;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.util.TranslationCollector;
import net.minecraft.network.chat.Component;

public class ConnectFourTexts {
    public static final TranslationCollector KEYS = new TranslationCollector(LoveTropics.ID + ".minigame.connect_four.");

    public static final Component IT_IS_YOUR_TURN = KEYS.add("your_turn", "It is your turn to place a block");

    public static final TranslationCollector.Fun1 TEAM_GOES_NEXT = KEYS.add1("teams_goes_next", "Team %s goes next");
}
