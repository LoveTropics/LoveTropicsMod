package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.LinkedList;

public final class CountdownPackageBehavior implements IGameBehavior {
	public static final Codec<CountdownPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.LONG.fieldOf("countdown").forGetter(c -> c.countdown / 20),
				TemplatedText.CODEC.fieldOf("warning").forGetter(c -> c.warning),
				MoreCodecs.arrayOrUnit(IGameBehavior.CODEC, IGameBehavior[]::new).fieldOf("behaviors").forGetter(c -> c.behaviors)
		).apply(instance, CountdownPackageBehavior::new);
	});

	private final long countdown;
	private final TemplatedText warning;
	private final IGameBehavior[] behaviors;

	private final GameEventListeners applyEvents = new GameEventListeners();

	private final LinkedList<QueueEntry> queue = new LinkedList<>();

	public CountdownPackageBehavior(long countdown, TemplatedText warning, IGameBehavior[] behaviors) {
		this.countdown = countdown * 20;
		this.warning = warning;
		this.behaviors = behaviors;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		EventRegistrar receiveEventRegistrar = events.redirect(t -> t == GamePackageEvents.APPLY_PACKAGE, applyEvents);
		for (IGameBehavior behavior : behaviors) {
			behavior.register(game, receiveEventRegistrar);
		}

		events.listen(GamePackageEvents.APPLY_PACKAGE, (player, sendingPlayer) -> {
			queue.add(new QueueEntry(countdown, player, sendingPlayer));
			return true;
		});

		events.listen(GamePhaseEvents.TICK, () -> {
			if (!queue.isEmpty()) {
				queue.removeIf(entry -> tickQueuedPackage(game, entry));
			}
		});
	}

	private boolean tickQueuedPackage(IGamePhase game, QueueEntry entry) {
		long remainingTicks = entry.time - game.ticks();
		if (remainingTicks <= 0) {
			return applyPackage(entry.player, entry.sendingPlayer);
		}

		if (remainingTicks % 20 == 0) {
			long remainingSeconds = remainingTicks / 20;
			IFormattableTextComponent timeText = new StringTextComponent(String.valueOf(remainingSeconds)).mergeStyle(TextFormatting.GOLD);
			entry.player.sendStatusMessage(warning.apply(timeText), true);
			entry.player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.MASTER, 0.8F, 1.0F);
		}

		return false;
	}

	private boolean applyPackage(ServerPlayerEntity player, @Nullable String sendingPlayer) {
		return applyEvents.invoker(GamePackageEvents.APPLY_PACKAGE).applyPackage(player, sendingPlayer);
	}

	static final class QueueEntry {
		final long time;
		final ServerPlayerEntity player;
		@Nullable
		final String sendingPlayer;

		QueueEntry(long time, ServerPlayerEntity player, @Nullable String sendingPlayer) {
			this.time = time;
			this.player = player;
			this.sendingPlayer = sendingPlayer;
		}
	}
}
