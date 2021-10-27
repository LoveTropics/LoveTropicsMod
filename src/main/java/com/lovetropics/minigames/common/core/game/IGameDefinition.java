package com.lovetropics.minigames.common.core.game;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Used as a discriminant for a registered minigame. Defines the logic of the
 * minigame as it is actively running, and provides methods to customize the
 * ruleset for the minigame such as maximum and minimum participants, game types
 * for each player type, dimension the minigame takes place in, etc.
 */
public interface IGameDefinition {
	/**
	 * The identifier for this minigame definition. Must be unique
	 * compared to other registered minigames.
	 *
	 * @return The identifier for this minigame definition.
	 */
	ResourceLocation getId();

	/**
	 * An identifier for telemetry usage, so that variants of games can share
	 * statistics. Defaults to the ID if not set in the JSON.
	 *
	 * @return The telemetry key for this minigame.
	 */
	default String getTelemetryKey() {
		return getId().getPath();
	}

	ITextComponent getName();

	@Nullable
	ITextComponent getSubtitle();

	/**
	 * Will not let you start the minigame without at least this amount of
	 * players registered for the polling minigame.
	 *
	 * @return The minimum amount of players required to start the minigame.
	 */
	default int getMinimumParticipantCount() {
		return 0;
	}

	/**
	 * Will only select up to this many participants to actually play
	 * in the started minigame. The rest of the players registered for
	 * the minigame will be slotted in as spectators where they can watch
	 * the minigame unfold.
	 *
	 * @return The maximum amount of players that can be participants in the
	 * minigame.
	 */
	default int getMaximumParticipantCount() {
		return Integer.MAX_VALUE;
	}

	IGamePhaseDefinition getPlayingPhase();

	default Optional<IGamePhaseDefinition> getWaitingPhase() {
		return Optional.empty();
	}
}
