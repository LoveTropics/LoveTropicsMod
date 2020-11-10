package com.lovetropics.minigames.common.game_actions;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Chat-caused event
 */
public class PollResultGameAction extends GameAction {
    private final String resultType;
    private final String title;
    private final List<PollEntry> entries;
    // TODO: Poll result action

    // UUID is not human readable
    // resultType is readable, ex: loot_package
    public PollResultGameAction(UUID uuid, String resultType, String triggerTime, final String title, final List<PollEntry> entries) {
        super(uuid, triggerTime);
        this.resultType = resultType;
        this.title = title;
        this.entries = entries;
    }

    @Override
    public boolean resolve(MinecraftServer server) {
        final StringBuilder builder = new StringBuilder()
                .append("Poll result: ")
                .append(title)
                .append(System.lineSeparator());

        for (int i = 0; i < entries.size(); i++) {
            final PollEntry entry = entries.get(i);

            if (i == 0) {
                builder.append("WINNER: ");
            }

            builder.append(entry.toString()).append(System.lineSeparator());
        }

        final ITextComponent pollResult = new StringTextComponent(builder.toString());
        server.sendMessage(pollResult);

        // TODO: Activate poll result action

        return true;
    }

    public static PollResultGameAction fromJson(final JsonObject obj) {
        final UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
        final String resultType = obj.get("chat_event_type").getAsString();
        final String triggerTime = obj.get("trigger_time").getAsString();

        final String title = obj.get("title").getAsString();
        final List<PollEntry> entries = Lists.newArrayList();

        for (final JsonElement element : obj.getAsJsonArray("options")) {
            final PollEntry entry = PollEntry.fromJson(element.getAsJsonObject());
            entries.add(entry);
        }

        entries.sort(Comparator.comparingInt(PollEntry::getResults).reversed());

        return new PollResultGameAction(uuid, resultType, triggerTime, title, entries);
    }

    public static class PollEntry {
        private final String title;
        private final int results;

        public PollEntry(final String title, final int results) {
            this.title = title;
            this.results = results;
        }

        public String getTitle() {
            return title;
        }

        public int getResults() {
            return results;
        }

        public static PollEntry fromJson(final JsonObject obj) {
            final String title = obj.get("title").getAsString();
            final int results = obj.get("results").getAsInt();

            return new PollEntry(title, results);
        }
    }
}
