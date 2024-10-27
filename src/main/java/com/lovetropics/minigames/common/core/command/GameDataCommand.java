package com.lovetropics.minigames.common.core.command;

import com.lovetropics.minigames.common.util.world.GameDataStorage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.commands.BossBarCommands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.ReadOnlyScoreInfo;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class GameDataCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // @formatter:off
        dispatcher.register(
                literal("gamedata").requires(source -> source.hasPermission(2))
                        .then(literal("get")
                                .then(argument("id", ResourceLocationArgument.id())
                                        .then(argument("player", EntityArgument.player())
                                                .then(argument("property", StringArgumentType.string())
                                                        .executes(context -> {
                                                            ServerLevel level = context.getSource().getLevel();
                                                            Tag tag = GameDataStorage.get(level)
                                                                    .get(ResourceLocationArgument.getId(context, "id"),
                                                                            EntityArgument.getPlayer(context, "player").getUUID(),
                                                                            StringArgumentType.getString(context, "property"));
                                                            int i;
                                                            if(tag instanceof NumericTag numericTag){
                                                                i = Mth.floor(numericTag.getAsDouble());
                                                            } else if (tag instanceof CollectionTag cTag) {
                                                                i = cTag.size();
                                                            } else if(tag instanceof CompoundTag cmpTag){
                                                                i = cmpTag.size();
                                                            } else {
                                                                if(!(tag instanceof StringTag)){
                                                                    throw new SimpleCommandExceptionType(new LiteralMessage("No property with that name exists")).create();
                                                                }
                                                                i = tag.getAsString().length();
                                                            }
                                                            context.getSource().sendSuccess(() -> NbtUtils.toPrettyComponent(tag), false);
                                                            return i;
                                                        })))
                                )
                        )
                        .then(literal("set")
                                .then(argument("id", ResourceLocationArgument.id())
                                        .then(argument("player", EntityArgument.player())
                                                .then(argument("property", StringArgumentType.string())
                                                        .then(argument("value", NbtTagArgument.nbtTag())
                                                                .executes(context -> {
                                                                    ServerLevel level = context.getSource().getLevel();
                                                                    GameDataStorage.get(level)
                                                                            .set(ResourceLocationArgument.getId(context, "id"),
                                                                                    EntityArgument.getPlayer(context, "player").getUUID(),
                                                                                    StringArgumentType.getString(context, "property"),
                                                                                    NbtTagArgument.getNbtTag(context, "value"));
                                                                    return 0;
                                                                }))
                                                                .then(literal("from")
                                                                        .then(literal("scoreboard")
                                                                                .then(argument("target", ScoreHolderArgument.scoreHolder())
                                                                                        .suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
                                                                                        .then(argument("objective", ObjectiveArgument.objective())
                                                                                                .executes(context -> {
                                                                                                    ServerLevel level = context.getSource().getLevel();
                                                                                                    ServerScoreboard scoreboard = level.getScoreboard();
                                                                                                    ReadOnlyScoreInfo info = scoreboard.getPlayerScoreInfo(ScoreHolderArgument.getName(context, "target"), ObjectiveArgument.getObjective(context, "objective"));
                                                                                                    if(info == null){
                                                                                                        throw new SimpleCommandExceptionType(new LiteralMessage("No scoreboard player / objective with that name exists")).create();
                                                                                                    }
                                                                                                    GameDataStorage.get(level)
                                                                                                            .set(ResourceLocationArgument.getId(context, "id"),
                                                                                                                    EntityArgument.getPlayer(context, "player").getUUID(),
                                                                                                                    StringArgumentType.getString(context, "property"),
                                                                                                                    IntTag.valueOf(info.value()));
                                                                                                    return 0;
                                                                                                })))))
                                                        ))))
        );
        // @formatter:on
    }
}
