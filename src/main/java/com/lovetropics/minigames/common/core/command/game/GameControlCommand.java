package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.GameManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class GameControlCommand {
    public static void register(final CommandDispatcher<CommandSource> dispatcher) {
        // @formatter:off
        dispatcher.register(
            literal("game")
                .then(argument("control", StringArgumentType.string())
                    .suggests((context, builder) -> {
                        IGameManager manager = GameManager.getInstance();
                        return ISuggestionProvider.suggest(manager.controlCommandsFor(context.getSource()), builder);
                    })
                    .executes(ctx -> {
                        String control = StringArgumentType.getString(ctx, "control");
                        IGameManager manager = GameManager.getInstance();
                        manager.invokeControlCommand(control, ctx.getSource());
                        return Command.SINGLE_SUCCESS;
                    })
                )
        );
        // @formatter:on
    }
}
