package com.lovetropics.minigames.common.core.integration.game_actions;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Chat-caused event
 */
public record ChatEventGameAction(String resultType, String title, List<PollEntry> entries) implements GameAction {
    public static final MapCodec<ChatEventGameAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("chat_event_type").forGetter(c -> c.resultType),
            Codec.STRING.fieldOf("title").forGetter(c -> c.title),
            MoreCodecs.sorted(PollEntry.CODEC.listOf(), Comparator.comparingInt(PollEntry::results).reversed()).fieldOf("options").forGetter(c -> c.entries)
    ).apply(i, ChatEventGameAction::new));

    @Override
    public boolean resolve(IGamePhase game, MinecraftServer server) {
        if (entries.isEmpty()) {
            return true;
        }

        PollEntry winner = entries.get(0);
        GamePackage winnerPackage = winner.asPackage();

        int votes = 0;
        for (PollEntry entry : entries) {
            votes += entry.results;
        }

        final int totalVotes = votes;
        Consumer<IGamePhase> preamble = g -> g.getAllPlayers().sendMessage(
                new TextComponent(this.title).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA)
                    .append(new TextComponent(
                            " just completed! After " + totalVotes + " votes, chat decided on something to happen... Do you trust them to have been nice?"
                    ).withStyle(ChatFormatting.GRAY))
        );

        InteractionResult result = game.invoker(GamePackageEvents.RECEIVE_PACKAGE).onReceivePackage(preamble, winnerPackage);
        return result == InteractionResult.SUCCESS;
    }

    public PollEntry winner() {
        return entries.get(0);
    }

    public record PollEntry(String key, String packageType, int results) {
        public static final Codec<PollEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("key").forGetter(c -> c.key),
                Codec.STRING.fieldOf("title").forGetter(c -> c.packageType),
                Codec.INT.fieldOf("results").forGetter(c -> c.results)
        ).apply(i, PollEntry::new));

        public GamePackage asPackage() {
            return new GamePackage(packageType, null, null);
        }
    }
}
