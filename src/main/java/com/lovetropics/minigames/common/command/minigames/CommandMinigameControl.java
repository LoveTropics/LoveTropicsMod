package com.lovetropics.minigames.common.command.minigames;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;

import java.util.stream.Stream;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class CommandMinigameControl {
    public static void register(final CommandDispatcher<CommandSource> dispatcher) {
        // @formatter:off
        dispatcher.register(
            literal("minigame")
                .then(argument("control", StringArgumentType.string())
                    .suggests((context, builder) -> {
                        IMinigameInstance minigame = MinigameManager.getInstance().getActiveOrPollingMinigame();
                        if (minigame != null) {
                            return ISuggestionProvider.suggest(minigame.getControlCommands().stream(), builder);
                        } else {
                            return ISuggestionProvider.suggest(Stream.empty(), builder);
                        }
                    })
                    .requires(src -> src.hasPermissionLevel(2))
                    .executes(ctx -> {
                        IMinigameInstance minigame = MinigameManager.getInstance().getActiveOrPollingMinigame();
                        if (minigame != null) {
                            String control = StringArgumentType.getString(ctx, "control");
                            minigame.invokeControlCommand(control, ctx.getSource());
                        }
                        return Command.SINGLE_SUCCESS;
                    })
                )
        );
        // @formatter:on
    }
}
