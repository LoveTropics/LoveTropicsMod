package com.lovetropics.minigames.common.command.minigames;

import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.lovetropics.minigames.common.minigames.statistics.StatisticsMap;
import com.lovetropics.minigames.common.techstack.MinigameResults;
import com.lovetropics.minigames.common.techstack.TechStack;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;

import static net.minecraft.command.Commands.literal;

public class CommandMinigameSendResults {
    public static void register(final CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            literal("minigame")
                .then(literal("fakeresults").requires(s -> s.hasPermissionLevel(2))
                    .executes(c -> {
                        final MinigameStatistics statistics = new MinigameStatistics();

                        StatisticsMap global = statistics.getGlobal();
                        global.set(StatisticKey.KILLS, 5);
                        global.set(StatisticKey.TOTAL_TIME, 121);

                        StatisticsMap player = statistics.forPlayer(c.getSource().asPlayer());
                        player.set(StatisticKey.PLACEMENT, 1);
                        player.set(StatisticKey.KILLS, 4);

                        TechStack.uploadMinigameResults(new MinigameResults(
                                "survive_the_tide_1",
                                "Survive The Tide I",
                                "Herobrine",
                                statistics,
                                System.currentTimeMillis() / 1000
                        ));

                        c.getSource().sendFeedback(new StringTextComponent("Nailed it"), true);

                        return 1;
                    })));
    }
}
