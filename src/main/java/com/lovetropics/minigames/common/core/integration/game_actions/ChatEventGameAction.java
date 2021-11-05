package com.lovetropics.minigames.common.core.integration.game_actions;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Chat-caused event
 */
public class ChatEventGameAction extends GameAction {
    public static final Codec<ChatEventGameAction> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                MoreCodecs.UUID_STRING.fieldOf("uuid").forGetter(c -> c.uuid),
                Codec.STRING.fieldOf("chat_event_type").forGetter(c -> c.resultType),
                TIME_CODEC.fieldOf("trigger_time").forGetter(c -> c.triggerTime),
                Codec.STRING.fieldOf("title").forGetter(c -> c.title),
                MoreCodecs.sorted(PollEntry.CODEC.listOf(), Comparator.comparingInt(PollEntry::getResults).reversed()).fieldOf("options").forGetter(c -> c.entries)
        ).apply(instance, ChatEventGameAction::new);
    });

    private final String resultType;
    private final String title;
    private final List<PollEntry> entries;

    // UUID is not human readable
    // resultType is readable, ex: loot_package
    public ChatEventGameAction(UUID uuid, String resultType, LocalDateTime triggerTime, final String title, final List<PollEntry> entries) {
        super(uuid, triggerTime);
        this.resultType = resultType;
        this.title = title;
        this.entries = entries;
    }

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
                new StringTextComponent(this.title).mergeStyle(TextFormatting.BOLD, TextFormatting.AQUA)
                    .appendSibling(new StringTextComponent(
                            " just completed! After " + totalVotes + " votes, chat decided on something to happen... Do you trust them to have been nice?"
                    ).mergeStyle(TextFormatting.GRAY))
        );

        ActionResultType result = game.invoker(GamePackageEvents.RECEIVE_PACKAGE).onReceivePackage(preamble, winnerPackage);
        return result == ActionResultType.SUCCESS;
    }

    public String getTitle() {
        return title;
    }

    public String getResultType() {
        return resultType;
    }

    public List<PollEntry> getEntries() {
        return entries;
    }

    public PollEntry getWinner() {
        return entries.get(0);
    }

    public static class PollEntry {
        public static final Codec<PollEntry> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codec.STRING.fieldOf("key").forGetter(c -> c.key),
                    Codec.STRING.fieldOf("title").forGetter(c -> c.packageType),
                    Codec.INT.fieldOf("results").forGetter(c -> c.results)
            ).apply(instance, PollEntry::new);
        });

        private final String key;
        private final String packageType;
        private final int results;

        public PollEntry(final String key, String packageType, final int results) {
            this.key = key;
            this.packageType = packageType;
            this.results = results;
        }

        public String getKey() {
            return key;
        }

        public String getPackageType() {
            return packageType;
        }

        public int getResults() {
            return results;
        }

        public GamePackage asPackage() {
            return new GamePackage(packageType, null, null);
        }
    }
}
