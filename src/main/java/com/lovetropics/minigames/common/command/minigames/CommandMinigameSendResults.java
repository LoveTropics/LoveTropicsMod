package com.lovetropics.minigames.common.command.minigames;

import com.lovetropics.minigames.common.techstack.ParticipantEntry;
import com.lovetropics.minigames.common.techstack.TechStack;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

import static net.minecraft.command.Commands.literal;

public class CommandMinigameSendResults {
    public static void register(final CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            literal("minigame")
                .then(literal("fakeresults").requires(s -> s.hasPermissionLevel(2))
                    .executes(c -> {
                        final String name = "Survive the tide";
                        final String host = "OMGChad";
                        final List<ParticipantEntry> participants = ParticipantEntry.fakeEntries();

                        TechStack.uploadMinigameResults(name, host, participants);

                        c.getSource().sendFeedback(new StringTextComponent("Nailed it"), true);

                        return 1;
                    })));
    }
}
