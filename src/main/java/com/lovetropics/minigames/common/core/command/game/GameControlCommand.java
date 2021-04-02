package com.lovetropics.minigames.common.core.command.game;

import com.lovetropics.minigames.common.core.game.IGameManager;
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
                        IGameManager manager = IGameManager.get();
                        CommandSource source = context.getSource();
                        return ISuggestionProvider.suggest(manager.getControlInvoker(source).list(source), builder);
                    })
                    .executes(ctx -> {
                        String control = StringArgumentType.getString(ctx, "control");
                        IGameManager manager = IGameManager.get();
                        CommandSource source = ctx.getSource();
                        manager.getControlInvoker(source).invoke(control, source);
                        return Command.SINGLE_SUCCESS;
                    })
                )
        );
        // @formatter:on
    }
}
