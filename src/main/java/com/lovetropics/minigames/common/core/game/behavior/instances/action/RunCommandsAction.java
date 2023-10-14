package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.state.DebugModeState;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Function;

public record RunCommandsAction(List<String> globalCommands, List<String> playerCommands) implements IGameBehavior {
    private static final Logger LOGGER = LogManager.getLogger(RunCommandsAction.class);

    private static final Codec<String> COMMAND_CODEC = Codec.STRING.xmap(
            command -> {
                if (command.startsWith("/")) {
                    command = command.substring(1);
                }
                return command;
            },
            Function.identity()
    );

    public static final Codec<RunCommandsAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            MoreCodecs.strictOptionalFieldOf(MoreCodecs.listOrUnit(COMMAND_CODEC), "global", List.of()).forGetter(RunCommandsAction::globalCommands),
            MoreCodecs.strictOptionalFieldOf(MoreCodecs.listOrUnit(COMMAND_CODEC), "player", List.of()).forGetter(RunCommandsAction::playerCommands)
    ).apply(i, RunCommandsAction::new));

    @Override
    public void register(IGamePhase game, EventRegistrar events) {
        CommandDispatcher<CommandSourceStack> dispatcher = game.getServer().getCommands().getDispatcher();
        CommandSourceStack source = createCommandSource(game);

        events.listen(GameActionEvents.APPLY, (context) -> executeCommands(dispatcher, source, globalCommands));

        events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target) -> {
            CommandSourceStack targetSource = source.withEntity(target).withPosition(target.position());
            return executeCommands(dispatcher, targetSource, playerCommands);
        });
    }

    private static boolean executeCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandSourceStack source, List<String> commands) {
        if (commands.isEmpty()) {
            return false;
        }
        for (String command : commands) {
            try {
                dispatcher.execute(command, source);
            } catch (CommandSyntaxException e) {
                LOGGER.error("Failed to execute command `{}` for {}", command, e);
                return false;
            }
        }
        return true;
    }

    private static CommandSourceStack createCommandSource(IGamePhase game) {
        boolean debugMode = game.getState().getOrNull(DebugModeState.KEY) != null;
        CommandSource source = debugMode ? game.getServer() : CommandSource.NULL;

        String name = game.getLobby().getMetadata().name();
        return new CommandSourceStack(source, Vec3.ZERO, Vec2.ZERO, game.getWorld(), Commands.LEVEL_OWNERS, name, Component.literal(name), game.getServer(), null);
    }
}
