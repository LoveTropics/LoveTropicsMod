package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Optional;

public final class GameEndEffectsBehavior implements IGameBehavior {
	public static final Codec<GameEndEffectsBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.LONG.fieldOf("stop_delay").forGetter(c -> c.stopDelay),
				MoreCodecs.long2Object(TemplatedText.CODEC).optionalFieldOf("scheduled_messages", new Long2ObjectOpenHashMap<>()).forGetter(c -> c.scheduledMessages),
				TemplatedText.CODEC.optionalFieldOf("title").forGetter(c -> Optional.ofNullable(c.title))
		).apply(instance, GameEndEffectsBehavior::new);
	});

	private final long stopDelay;
	private final Long2ObjectMap<TemplatedText> scheduledMessages;
	@Nullable
	private final TemplatedText title;

	private boolean ended;
	private long stopTime;

	private ITextComponent winner;

	public GameEndEffectsBehavior(long stopDelay, Long2ObjectMap<TemplatedText> scheduledMessages, Optional<TemplatedText> title) {
		this.stopDelay = stopDelay;
		this.scheduledMessages = scheduledMessages;
		this.title = title.orElse(null);
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GameLogicEvents.WIN_TRIGGERED, winner -> {
			if (!ended) {
				this.triggerEnd(game, winner);
			}
		});

		events.listen(GamePhaseEvents.TICK, () -> {
			if (this.ended) {
				this.tickEnded(game);
			}
		});
	}

	private void triggerEnd(IGamePhase game, ITextComponent winner) {
		if (winner.getStyle().getColor() != null) {
			this.winner = winner;
		} else {
			this.winner = winner.deepCopy().mergeStyle(TextFormatting.AQUA);
		}

		this.ended = true;

		if (this.title != null) {
			ITextComponent title = this.title.apply(winner);
			PlayerSet players = game.getAllPlayers();
			players.sendPacket(new STitlePacket(10, 3 * 20, 10));
			players.sendPacket(new STitlePacket(STitlePacket.Type.SUBTITLE, title));
		}
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
