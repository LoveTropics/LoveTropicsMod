package com.lovetropics.minigames.common.minigames.definitions.survive_the_tide;

import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.dimension.DimensionUtils;
import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.List;

/**
 * Definition implementation for the Island Royale minigame.
 *
 * Will resolve minigame features and logic in worldUpdate() method
 * later on.
 */
public class SurviveTheTideMinigameDefinition extends ForgeRegistryEntry<IMinigameDefinition> implements IMinigameDefinition {
    public static ResourceLocation ID = Util.resource("survive_the_tide");
    private String displayName = TropicraftLangKeys.MINIGAME_SURVIVE_THE_TIDE;

    public SurviveTheTideMinigameDefinition() {
    }

    @Override
    public List<IMinigameBehavior> getBehaviours()
    {
        return null;
    }

    @Override
    public ActionResult<ITextComponent> canStartMinigame(final MinecraftServer server) {
        ServerWorld world = DimensionManager.getWorld(server, this.getDimension(), false, false);

        if (world != null) {
            if (world.getPlayers().size() <= 0) {
                DimensionManager.unloadWorld(world);
                return new ActionResult<>(ActionResultType.FAIL, new StringTextComponent("The Survive the Tide dimension was not unloaded. Begun unloading, please try again in a few seconds.").applyTextStyle(TextFormatting.RED));
            }

            return new ActionResult<>(ActionResultType.FAIL, new StringTextComponent("Cannot start minigame as players are in Survive The Tide dimension. Make them teleport out first.").applyTextStyle(TextFormatting.RED));
        }

        return new ActionResult<>(ActionResultType.SUCCESS, new StringTextComponent(""));
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public String getUnlocalizedName() {
        return this.displayName;
    }

    @Override
    public DimensionType getDimension() {
        return DimensionUtils.SURVIVE_THE_TIDE_DIMENSION;
    }

    @Override
    public GameType getParticipantGameType() {
        return GameType.ADVENTURE;
    }

    @Override
    public GameType getSpectatorGameType() {
        return GameType.SPECTATOR;
    }

    @Override
    public BlockPos getSpectatorPosition() {
        return ConfigLT.minigame_SurviveTheTide_spectatorPosition;
    }

    @Override
    public BlockPos getPlayerRespawnPosition(IMinigameInstance instance) {
        return ConfigLT.minigame_SurviveTheTide_respawnPosition;
    }

    @Override
    public int getMinimumParticipantCount() {
        return ConfigLT.MINIGAME_SURVIVE_THE_TIDE.minimumPlayerCount.get();
    }

    @Override
    public int getMaximumParticipantCount() {
        return ConfigLT.MINIGAME_SURVIVE_THE_TIDE.maximumPlayerCount.get();
    }
}