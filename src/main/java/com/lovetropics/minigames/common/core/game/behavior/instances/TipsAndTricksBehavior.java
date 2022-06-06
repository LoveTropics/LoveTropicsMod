package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.*;

public final class TipsAndTricksBehavior implements IGameBehavior {
    public static final Codec<TipsAndTricksBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
            MoreCodecs.TEXT.listOf().fieldOf("texts").forGetter(b -> b.texts),
            Codec.INT.fieldOf("time_between_tips").forGetter(b -> b.timeBetweenTips)
    ).apply(i, TipsAndTricksBehavior::new));

    private final List<Component> texts;
    private final int timeBetweenTips;

    // Mutable copy that is removed from as the game progresses
    private List<Component> remainingTexts;
    private long startTime;

    public TipsAndTricksBehavior(List<Component> texts, int timeBetweenTips) {
        this.texts = texts;
        this.timeBetweenTips = timeBetweenTips;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(GamePhaseEvents.START, () -> {
            // Copy and randomize tips&tricks
            this.remainingTexts = new ArrayList<>(this.texts);
            Collections.shuffle(this.remainingTexts);

            this.startTime = game.ticks();
        });

        events.listen(GamePhaseEvents.TICK, () -> {
            if ((game.ticks() - this.startTime) % this.timeBetweenTips == 0) {
                if (!this.remainingTexts.isEmpty()) {
                    Component text = this.remainingTexts.remove(0);
                    game.getAllPlayers().sendMessage(text);
                }
            }
        });
    }
}
