package com.lovetropics.minigames.common.game_actions;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.server.MinecraftServer;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Chat-caused event
 */
public class ChatEventGameAction extends GameAction {
    private final String resultType;
    private final String title;
    private final List<PollEntry> entries;

    // UUID is not human readable
    // resultType is readable, ex: loot_package
    public ChatEventGameAction(UUID uuid, String resultType, String triggerTime, final String title, final List<PollEntry> entries) {
        super(uuid, triggerTime);
        this.resultType = resultType;
        this.title = title;
        this.entries = entries;
    }

    @Override
    public boolean resolve(IMinigameInstance minigame, MinecraftServer server) {
        if (entries.isEmpty()) {
            return true;
        }

        boolean resolved = false;
        for (IMinigameBehavior behavior : minigame.getBehaviors()) {
            resolved |= behavior.onChatEventReceived(minigame, this);
        }

        return resolved;
    }

    public static ChatEventGameAction fromJson(final JsonObject obj) {
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

        return new ChatEventGameAction(uuid, resultType, triggerTime, title, entries);
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
        private final String key;
        private final String title;
        private final int results;

        public PollEntry(final String key, final String title, final int results) {
            this.key = key;
            this.title = title;
            this.results = results;
        }

        public String getKey() {
            return key;
        }

        public String getTitle() {
            return title;
        }

        public int getResults() {
            return results;
        }

        public static PollEntry fromJson(final JsonObject obj) {
            final String key = obj.get("key").getAsString();
            final String title = obj.get("title").getAsString();
            final int results = obj.get("results").getAsInt();

            return new PollEntry(key, title, results);
        }
    }
}
