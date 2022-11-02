package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.GameProgressionState;
import com.lovetropics.minigames.common.core.game.state.ProgressionPeriod;
import com.lovetropics.minigames.common.core.game.state.weather.GameWeatherState;
import com.lovetropics.minigames.common.core.game.util.GameSidebar;
import com.lovetropics.minigames.common.core.game.util.GlobalGameWidgets;
import com.lovetropics.minigames.common.core.game.weather.WeatherEventType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

// TODO: make it generic and data-driven
public class SttSidebarBehavior implements IGameBehavior {
	public static final Codec<SttSidebarBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			ProgressionPeriod.CODEC.fieldOf("safe_period").forGetter(b -> b.safePeriod),
			ProgressionPeriod.CODEC.fieldOf("tide_rising_period").forGetter(b -> b.tideRisingPeriod),
			ProgressionPeriod.CODEC.fieldOf("iceberg_growth_period").forGetter(b -> b.icebergGrowthPeriod),
			ProgressionPeriod.CODEC.fieldOf("explosive_storm_period").forGetter(b -> b.explosiveStormPeriod)
	).apply(i, SttSidebarBehavior::new));

	private final ProgressionPeriod safePeriod;
	private final ProgressionPeriod tideRisingPeriod;
	private final ProgressionPeriod icebergGrowthPeriod;
	private final ProgressionPeriod explosiveStormPeriod;

	private GlobalGameWidgets widgets;
	private GameSidebar sidebar;

	private IGamePhase game;
	private GameProgressionState progression;
	private GameWeatherState weather;

	private int initialPlayerCount;

	private long lastUpdateTime;
	private boolean dirty;

	public SttSidebarBehavior(ProgressionPeriod safePeriod, ProgressionPeriod tideRisingPeriod, ProgressionPeriod icebergGrowthPeriod, ProgressionPeriod explosiveStormPeriod) {
		this.safePeriod = safePeriod;
		this.tideRisingPeriod = tideRisingPeriod;
		this.icebergGrowthPeriod = icebergGrowthPeriod;
		this.explosiveStormPeriod = explosiveStormPeriod;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		this.game = game;
		widgets = GlobalGameWidgets.registerTo(game, events);

		progression = game.getState().getOrNull(GameProgressionState.KEY);
		weather = game.getState().getOrNull(GameWeatherState.KEY);

		events.listen(GamePhaseEvents.START, () -> {
			sidebar = widgets.openSidebar(game.getDefinition().getName().copy().withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
			initialPlayerCount = game.getParticipants().size();
		});

		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> dirty = true);

		events.listen(GamePhaseEvents.TICK, () -> {
			if (dirty || game.ticks() - lastUpdateTime > SharedConstants.TICKS_PER_SECOND) {
				sidebar.set(buildSidebar());
				lastUpdateTime = game.ticks();
				dirty = false;
			}
		});
	}

	private Component[] buildSidebar() {
		return new Component[]{
				new TextComponent("Weather: ").append(weatherName()),
				phaseState(),
				TextComponent.EMPTY,
				playersState()
		};
	}

	private Component weatherName() {
		WeatherEventType type = weather.getEventType();
		if (type == null) {
			return new TextComponent("Clear");
		}

		return switch (type) {
			case HEAVY_RAIN -> new TextComponent("Heavy Rain").withStyle(ChatFormatting.BLUE);
			case ACID_RAIN -> new TextComponent("Acid Rain").withStyle(ChatFormatting.GREEN);
			case HAIL -> new TextComponent("Hail").withStyle(ChatFormatting.BLUE);
			case HEATWAVE -> new TextComponent("Heat Wave").withStyle(ChatFormatting.YELLOW);
			case SANDSTORM -> new TextComponent("Sandstorm").withStyle(ChatFormatting.YELLOW);
			case SNOWSTORM -> new TextComponent("Snowstorm").withStyle(ChatFormatting.WHITE);
		};
	}

	private Component phaseState() {
		if (progression.is(safePeriod)) {
			return new TextComponent("PVP disabled").withStyle(ChatFormatting.YELLOW);
		} else if (progression.is(tideRisingPeriod)) {
			return new TextComponent("The tide is rising!").withStyle(ChatFormatting.RED);
		} else if (progression.is(icebergGrowthPeriod)) {
			return new TextComponent("Icebergs are forming!").withStyle(ChatFormatting.AQUA);
		} else if (progression.is(explosiveStormPeriod)) {
			return new TextComponent("Explosive storm closing!").withStyle(ChatFormatting.RED);
		}
		return TextComponent.EMPTY;
	}

	private Component playersState() {
		int playerCount = game.getParticipants().size();
		return new TextComponent(playerCount + "/" + initialPlayerCount + " players").withStyle(ChatFormatting.GRAY);
	}
}
