package com.lovetropics.minigames.common.content.biodiversity_blitz.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import net.minecraft.server.level.ServerPlayer;

public class BbUtils {
    // Helper to map from a player->plot structure to a plot->{players} structure
    public static Multimap<Plot, ServerPlayer> reassociate(PlotsState state, PlayerSet set) {
        Multimap<Plot, ServerPlayer> map = HashMultimap.create();
        for (ServerPlayer player : set) {
            Plot plot = state.getPlotFor(player);
            if (plot != null) {
                map.put(plot, player);
            }
        }

        return map;
    }
}
