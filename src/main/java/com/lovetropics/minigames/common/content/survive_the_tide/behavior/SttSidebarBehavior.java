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

// TODO: make it generic and data-driven
public class SttSidebarBehavior implements IGameBehavior {
	public static final Codec<SttSidebarBehavior> CODEC = Codec.unit(SttSidebarBehavior::new);

	private GlobalGameWidgets widgets;
	private GameSidebar sidebar;

	private IGamePhase game;
	private GamePhaseState phases;
	private GameWeatherState weather;

	private long lastUpdateTime;
	private boolean dirty;

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		this.game = game;
		widgets = GlobalGameWidgets.registerTo(game, events);

		phases = game.getState().getOrNull(GamePhaseState.KEY);
		weather = game.getState().getOrNull(GameWeatherState.KEY);

		events.listen(GamePhaseEvents.START, () -> {
			sidebar = widgets.openSidebar(game.getDefinition().getName());
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

	private String[] buildSidebar() {
		return new String[] {
				"Weather: " + weatherName(),
				phaseState(),
				"",
				playersState()
		};
	}

	private String weatherName() {
		WeatherEventType type = weather.getEventType();
		if (type == null) {
			return "Clear";
		}

		return switch (type) {
			case HEAVY_RAIN -> ChatFormatting.BLUE + "Heavy Rain";
			case ACID_RAIN -> ChatFormatting.GREEN + "Acid Rain";
			case HAIL -> ChatFormatting.BLUE + "Hail";
			case HEATWAVE -> ChatFormatting.YELLOW + "Heat Wave";
			case SANDSTORM -> ChatFormatting.YELLOW + "Sandstorm";
			case SNOWSTORM -> ChatFormatting.WHITE + "Snowstorm";
		};
	}

	private String phaseState() {
		GamePhase phase = phases.get();
		String percent = ChatFormatting.GRAY + "(" + Math.round(phases.progress() * 100f) + "%)";
		return switch (phase.key()) {
			case "phase0", "phase1" -> ChatFormatting.YELLOW + "PVP disabled";
			case "phase2", "phase3" -> ChatFormatting.RED + "The tide is rising!";
			case "phase4" -> ChatFormatting.AQUA + "Icebergs are forming! " + percent;
			case "phase5" -> ChatFormatting.RED + "Explosive border closing! " + percent;
		};
	}

	private String playersState() {
		return ChatFormatting.GRAY.toString() + game.getParticipants().size() + " remaining";
	}
}
