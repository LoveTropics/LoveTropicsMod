package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.map.IMinigameMapProvider;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Map;

/**
 * Used as a discriminant for a registered minigame. Defines the logic of the
 * minigame as it is actively running, and provides methods to customize the
 * ruleset for the minigame such as maximum and minimum participants, game types
 * for each player type, dimension the minigame takes place in, etc.
 */
public interface IMinigameDefinition
{
    IMinigameMapProvider getMapProvider();

    Map<IMinigameBehaviorType<?>, IMinigameBehavior> createBehaviors();

    /**
     * The identifier for this minigame definition. Must be unique
     * compared to other registered minigames.
     * @return The identifier for this minigame definition.
     */
    ResourceLocation getID();

    /**
     * Used within messages sent to players as the minigame starts, stops, etc.
     * @return The unlocalized key string for the name of this minigame.
     */
    String getUnlocalizedName();

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
