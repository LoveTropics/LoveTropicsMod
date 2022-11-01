package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.GamePhase;
import com.lovetropics.minigames.common.core.game.state.GamePhaseState;
import com.lovetropics.minigames.common.core.game.state.weather.GameWeatherState;
import com.lovetropics.minigames.common.core.game.util.GameSidebar;
import com.lovetropics.minigames.common.core.game.util.GlobalGameWidgets;
import com.lovetropics.minigames.common.core.game.weather.WeatherEventType;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

// TODO: make it generic and data-driven
public class SttSidebarBehavior implements IGameBehavior {
	public static final Codec<SttSidebarBehavior> CODEC = Codec.unit(SttSidebarBehavior::new);

	private GlobalGameWidgets widgets;
	private GameSidebar sidebar;

	private IGamePhase game;
	private GamePhaseState phases;
	private GameWeatherState weather;

	private int initialPlayerCount;

	private long lastUpdateTime;
	private boolean dirty;

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		this.game = game;
		widgets = GlobalGameWidgets.registerTo(game, events);

		phases = game.getState().getOrNull(GamePhaseState.KEY);
		weather = game.getState().getOrNull(GameWeatherState.KEY);

		events.listen(GamePhaseEvents.START, () -> {
			sidebar = widgets.openSidebar(game.getDefinition().getName().copy().withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
			initialPlayerCount = game.getParticipants().size();
		});

		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> dirty = true);
		events.listen(GameLogicEvents.PHASE_CHANGE, (phase, lastPhase) -> dirty = true);

		events.listen(GamePhaseEvents.TICK, () -> {
			if (dirty || game.ticks() - lastUpdateTime > SharedConstants.TICKS_PER_SECOND) {
				sidebar.set(buildSidebar());
				lastUpdateTime = game.ticks();
				dirty = false;
			}
		});
	}

	private Component[] buildSidebar() {
		return new Component[] {
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
		GamePhase phase = phases.get();
		return switch (phase.key()) {
			case "phase0", "phase1" -> new TextComponent("PVP disabled").withStyle(ChatFormatting.YELLOW);
			case "phase2", "phase3" -> new TextComponent("The tide is rising!").withStyle(ChatFormatting.RED);
			case "phase4" -> new TextComponent("Icebergs are forming!" ).withStyle(ChatFormatting.AQUA);
			case "phase5" -> new TextComponent("Explosive border closing!").withStyle(ChatFormatting.RED);
			default -> TextComponent.EMPTY;
		};
	}

	private Component playersState() {
		int playerCount = game.getParticipants().size();
		return new TextComponent(playerCount + "/" + initialPlayerCount + " players").withStyle(ChatFormatting.GRAY);
	}
}
