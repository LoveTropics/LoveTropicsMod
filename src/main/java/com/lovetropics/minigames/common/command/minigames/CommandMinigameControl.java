package com.lovetropics.minigames.common.command.minigames;

import com.lovetropics.minigames.common.minigames.IMinigameManager;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class CommandMinigameControl {
    public static void register(final CommandDispatcher<CommandSource> dispatcher) {
        // @formatter:off
        dispatcher.register(
            literal("minigame")
                .then(argument("control", StringArgumentType.string())
                    .suggests((context, builder) -> {
                        IMinigameManager manager = MinigameManager.getInstance();
                        return ISuggestionProvider.suggest(manager.getControlCommands().stream(), builder);
                    })
                    .requires(src -> src.hasPermissionLevel(2))
                    .executes(ctx -> {
                        String control = StringArgumentType.getString(ctx, "control");
                        IMinigameManager manager = MinigameManager.getInstance();
                        manager.invokeControlCommand(control, ctx.getSource());
                        return Command.SINGLE_SUCCESS;
                    })
                )
        );
        // @formatter:on
    }
}
