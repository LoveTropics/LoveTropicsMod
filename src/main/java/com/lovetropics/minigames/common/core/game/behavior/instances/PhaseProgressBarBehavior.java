package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.GamePhase;
import com.lovetropics.minigames.common.core.game.state.GamePhaseState;
import com.lovetropics.minigames.common.core.game.util.GameBossBar;
import com.lovetropics.minigames.common.core.game.util.GlobalGameWidgets;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

import java.util.Map;

public class PhaseProgressBarBehavior implements IGameBehavior {
	private static final int UPDATE_INTERVAL = SharedConstants.TICKS_PER_SECOND / 2;

	public static final Codec<PhaseProgressBarBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.unboundedMap(GamePhase.CODEC, Style.CODEC).fieldOf("phases").forGetter(b -> b.phases)
	).apply(i, PhaseProgressBarBehavior::new));

	private final Map<GamePhase, Style> phases;

	private GameBossBar bossBar;
	private GamePhaseState phaseState;

	public PhaseProgressBarBehavior(Map<GamePhase, Style> phases) {
		this.phases = phases;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		phaseState = game.getState().getOrThrow(GamePhaseState.KEY);

		GlobalGameWidgets widgets = GlobalGameWidgets.registerTo(game, events);

		events.listen(GamePhaseEvents.TICK, () -> {
			if (game.ticks() % UPDATE_INTERVAL != 0) {
				return;
			}

			GamePhase phase = phaseState.get();
			Style style = phases.get(phase);
			if (style == null) {
				if (bossBar != null) {
					bossBar.close();
					bossBar = null;
				}
				return;
			}

			updateVisibleBossBar(widgets, style);
		});
	}

	private void updateVisibleBossBar(GlobalGameWidgets widgets, Style style) {
		Component text = getTitle(style);
		if (bossBar == null) {
			bossBar = widgets.openBossBar(text, style.color, BossEvent.BossBarOverlay.PROGRESS);
		} else {
			bossBar.setTitle(text);
			bossBar.setStyle(style.color, BossEvent.BossBarOverlay.PROGRESS);
		}
		float progress = phaseState.progress();
		bossBar.setProgress(style.reversed ? 1.0f - progress : progress);
	}

	private Component getTitle(Style style) {
		int ticksLeft = phaseState.ticksLeft();
		if (ticksLeft == GamePhaseState.NO_TIME_ESTIMATE) {
			return style.text;
		}

		int secondsLeft = Mth.positiveCeilDiv(ticksLeft, SharedConstants.TICKS_PER_SECOND);
		return style.text.copy().append(new TextComponent(" (" + Util.formatMinutesSeconds(secondsLeft) + " left)").withStyle(ChatFormatting.GRAY));
	}

	private record Style(Component text, BossEvent.BossBarColor color, boolean reversed) {
		private static final Codec<BossEvent.BossBarColor> BOSS_BAR_COLOR_CODEC = MoreCodecs.stringVariants(BossEvent.BossBarColor.values(), BossEvent.BossBarColor::getName);

		public static final Codec<Style> CODEC = RecordCodecBuilder.create(i -> i.group(
				MoreCodecs.TEXT.fieldOf("text").forGetter(Style::text),
				BOSS_BAR_COLOR_CODEC.optionalFieldOf("color", BossEvent.BossBarColor.WHITE).forGetter(Style::color),
				Codec.BOOL.optionalFieldOf("reversed", false).forGetter(Style::reversed)
		).apply(i, Style::new));
	}
}
