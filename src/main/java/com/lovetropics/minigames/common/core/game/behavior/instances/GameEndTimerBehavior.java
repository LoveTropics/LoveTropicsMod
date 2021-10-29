package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.text.ITextComponent;

public final class GameEndTimerBehavior implements IGameBehavior {
	public static final Codec<GameEndTimerBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.LONG.fieldOf("stop_delay").forGetter(c -> c.stopDelay),
				MoreCodecs.long2Object(TemplatedText.CODEC).optionalFieldOf("scheduled_messages", new Long2ObjectOpenHashMap<>()).forGetter(c -> c.scheduledMessages)
		).apply(instance, GameEndTimerBehavior::new);
	});

	private final long stopDelay;
	private final Long2ObjectMap<TemplatedText> scheduledMessages;

	private boolean ended;
	private long stopTime;

	private ITextComponent winner;

	public GameEndTimerBehavior(long stopDelay, Long2ObjectMap<TemplatedText> scheduledMessages) {
		this.stopDelay = stopDelay;
		this.scheduledMessages = scheduledMessages;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GameLogicEvents.WIN_TRIGGERED, winnerName -> {
			this.winner = winnerName;
			this.ended = true;
		});

		events.listen(GamePhaseEvents.TICK, () -> {
			if (this.ended) {
				this.tickEnded(game);
			}
		});
	}

	private void tickEnded(IGamePhase game) {
		this.sendScheduledMessages(game, stopTime);

		if (stopTime == stopDelay) {
			game.requestStop(GameStopReason.finished());
		}

		stopTime++;
	}

	private void sendScheduledMessages(IGamePhase game, long stopTime) {
		TemplatedText message = this.scheduledMessages.remove(stopTime);
		if (message != null) {
			game.getAllPlayers().sendMessage(message.apply(winner));
		}
	}
}
