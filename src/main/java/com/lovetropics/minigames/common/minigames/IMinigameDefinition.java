package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;

/**
 * Used as a discriminant for a registered minigame. Defines the logic of the
 * minigame as it is actively running, and provides methods to customize the
 * ruleset for the minigame such as maximum and minimum participants, game types
 * for each player type, dimension the minigame takes place in, etc.
 */
public interface IMinigameDefinition extends IForgeRegistryEntry<IMinigameDefinition>
{
    List<IMinigameBehavior> getBehaviours();

    default ActionResult<ITextComponent> canStartMinigame(final MinecraftServer server) {
        return new ActionResult<>(ActionResultType.SUCCESS, new StringTextComponent(""));
    }

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
     * The targeted dimension you'd like this minigame to teleport players to
     * when they join as players or spectators.
     * @return The dimension type players are teleported to when joining.
     */
    DimensionType getDimension();

    /**
     * Set when the minigame starts and you are a participant.
     * @return The game type players are set to when active participants of the minigame.
     */
    GameType getParticipantGameType();

    /**
     * Set when the minigame starts and you are a spectator.
     * @return The game type players are set to when they are considered spectators.
     */
    GameType getSpectatorGameType();

    /**
     * Relative to the dimension world specified by the dimension type.
     * @return The position spectators start at when the minigame starts.
     */
    BlockPos getSpectatorPosition();

    /**
     * Relative to the dimension world specified by the dimension type.
     * @param instance The instance of the running minigame.
     * @return The block position for players to respawn at on death.
     */
    BlockPos getPlayerRespawnPosition(IMinigameInstance instance);

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
