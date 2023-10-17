package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.GameProgressionState;
import com.lovetropics.minigames.common.core.game.state.ProgressionPeriod;
import com.lovetropics.minigames.common.core.game.util.GameBossBar;
import com.lovetropics.minigames.common.core.game.util.GlobalGameWidgets;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

import javax.annotation.Nullable;
import java.util.List;

public class ProgressBarBehavior implements IGameBehavior {
	private static final int UPDATE_INTERVAL = SharedConstants.TICKS_PER_SECOND / 2;

	public static final MapCodec<ProgressBarBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Entry.CODEC.listOf().fieldOf("entries").forGetter(b -> b.entries)
	).apply(i, ProgressBarBehavior::new));

	private final List<Entry> entries;

	private GameBossBar bossBar;
	private GameProgressionState progression;

	public ProgressBarBehavior(List<Entry> entries) {
		this.entries = entries;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		progression = game.getState().getOrThrow(GameProgressionState.KEY);

		GlobalGameWidgets widgets = GlobalGameWidgets.registerTo(game, events);

		events.listen(GamePhaseEvents.TICK, () -> {
			if (game.ticks() % UPDATE_INTERVAL != 0) {
				return;
			}

			Entry entry = getActiveEntry();
			if (entry == null) {
				if (bossBar != null) {
					bossBar.close();
					bossBar = null;
				}
				return;
			}

			updateVisibleBossBar(widgets, entry);
		});
	}

	@Nullable
	private Entry getActiveEntry() {
		for (Entry entry : entries) {
			if (progression.is(entry.period())) {
				return entry;
			}
		}
		return null;
	}

	private void updateVisibleBossBar(GlobalGameWidgets widgets, Entry entry) {
		Component text = getTitle(entry);
		if (bossBar == null) {
			bossBar = widgets.openBossBar(text, entry.color, BossEvent.BossBarOverlay.PROGRESS);
		} else {
			bossBar.setTitle(text);
			bossBar.setStyle(entry.color, BossEvent.BossBarOverlay.PROGRESS);
		}
		float progress = progression.progressIn(entry.period());
		bossBar.setProgress(entry.reversed ? 1.0f - progress : progress);
	}

	private Component getTitle(Entry entry) {
		if (!entry.includeTime) {
			return entry.text;
		}

		int endTime = entry.period.end().resolve(progression);
		int secondsLeft = Mth.positiveCeilDiv(endTime - progression.time(), SharedConstants.TICKS_PER_SECOND);
		return entry.text.copy().append(Component.literal(" (" + Util.formatMinutesSeconds(secondsLeft) + " left)").withStyle(ChatFormatting.GRAY));
	}

	private record Entry(ProgressionPeriod period, Component text, BossEvent.BossBarColor color, boolean reversed, boolean includeTime) {
		private static final Codec<BossEvent.BossBarColor> BOSS_BAR_COLOR_CODEC = MoreCodecs.stringVariants(BossEvent.BossBarColor.values(), BossEvent.BossBarColor::getName);

		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
				ProgressionPeriod.CODEC.fieldOf("period").forGetter(Entry::period),
				ExtraCodecs.COMPONENT.fieldOf("text").forGetter(Entry::text),
				BOSS_BAR_COLOR_CODEC.optionalFieldOf("color", BossEvent.BossBarColor.WHITE).forGetter(Entry::color),
				Codec.BOOL.optionalFieldOf("reversed", false).forGetter(Entry::reversed),
				Codec.BOOL.optionalFieldOf("include_time", false).forGetter(Entry::includeTime)
		).apply(i, Entry::new));
	}
}
