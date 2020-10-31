package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.minigames.behaviours.BehaviorMap;
import com.lovetropics.minigames.common.minigames.map.IMinigameMapProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Used as a discriminant for a registered minigame. Defines the logic of the
 * minigame as it is actively running, and provides methods to customize the
 * ruleset for the minigame such as maximum and minimum participants, game types
 * for each player type, dimension the minigame takes place in, etc.
 */
public interface IMinigameDefinition
{
    IMinigameMapProvider getMapProvider();

    BehaviorMap createBehaviors();

    /**
     * The identifier for this minigame definition. Must be unique
     * compared to other registered minigames.
     * @return The identifier for this minigame definition.
     */
    ResourceLocation getID();

	/**
	 * An identifier for telemetry usage, so that variants of games can share
	 * statistics. Defaults to the ID if not set in the JSON.
	 * 
	 * @return The telemetry key for this minigame.
	 */
    String getTelemetryKey();

    /**
     * Used within messages sent to players as the minigame starts, stops, etc.
     * @return The unlocalized key string for the name of this minigame.
     */
    String getUnlocalizedName();

    default ITextComponent getName() {
        return new TranslationTextComponent(getUnlocalizedName());
    }

    /**
     * Will not let you start the minigame without at least this amount of
     * players registered for the polling minigame.
     *
     * @return The minimum amount of players required to start the minigame.
     */
    int getMinimumParticipantCount();

    /**
     * Will only select up to this many participants to actually play
     * in the started minigame. The rest of the players registered for
     * the minigame will be slotted in as spectators where they can watch
     * the minigame unfold.
     *
     * @return The maximum amount of players that can be participants in the
     * minigame.
     */
    int getMaximumParticipantCount();
}
