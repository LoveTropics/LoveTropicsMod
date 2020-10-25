package com.lovetropics.minigames.common.command.minigames;

import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.IMinigameManager;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.MutablePlayerSet;
import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.lovetropics.minigames.common.minigames.statistics.StatisticsMap;
import com.lovetropics.minigames.common.telemetry.MinigameInstanceTelemetry;
import com.lovetropics.minigames.common.telemetry.Telemetry;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

import static net.minecraft.command.Commands.literal;

public class CommandMinigameSendResults {
    public static void register(final CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            literal("minigame")
                .then(literal("fakeresults").requires(s -> s.hasPermissionLevel(2))
                    .executes(c -> {
                        ServerPlayerEntity player = c.getSource().asPlayer();

                        IMinigameManager minigameManager = MinigameManager.getInstance();
                        IMinigameDefinition minigame = minigameManager.getAllMinigames().iterator().next();

                        MinigameInstanceTelemetry telemetry = Telemetry.INSTANCE.openMinigame(minigame, PlayerKey.from(player));

                        MutablePlayerSet participants = new MutablePlayerSet(c.getSource().getServer());
                        participants.add(player);

                        // start the game with 1 participant
                        telemetry.start(participants);

                        // remove the participant
                        participants.remove(player);

                        // finish with statistics
                        final MinigameStatistics statistics = new MinigameStatistics();

                        StatisticsMap global = statistics.getGlobal();
                        global.set(StatisticKey.KILLS, 5);
                        global.set(StatisticKey.TOTAL_TIME, 121);

                        StatisticsMap playerStatistics = statistics.forPlayer(player);
                        playerStatistics.set(StatisticKey.PLACEMENT, 1);
                        playerStatistics.set(StatisticKey.KILLS, 4);

                        telemetry.finish(statistics);

                        c.getSource().sendFeedback(new StringTextComponent("Nailed it"), true);

                        return 1;
                    })));
    }
}
